package com.visuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu com abas, animacao de abrir/fechar, sliders suaves e troca de tecla.
 * O mouse e o teclado sao lidos por polling (GLFW) pra nao depender de
 * assinaturas de eventos que mudam entre versoes.
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 430, H = 250;
    private int px, py;
    private String tab = "HUD";
    private Feature selected;
    private Setting dragging;
    private Feature listening;
    private boolean prevDown;
    private final boolean[] prevKeys = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private final Map<Feature, Float> toggleAnim = new HashMap<>();
    private final long openStart = System.nanoTime();
    private long closeStart;
    private boolean closing;

    public ClickGuiScreen() { super(Text.literal("Visuals")); }

    public boolean isListening() { return listening != null; }

    @Override
    protected void init() {
        long h = client.getWindow().getHandle();
        prevDown = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++)
            prevKeys[k] = GLFW.glfwGetKey(h, k) == GLFW.GLFW_PRESS;
        selected = first(tab);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        Config.save();
        if (closing) return;
        if (Features.MENU.mode("Animacao") == 3) { super.close(); return; }
        closing = true;
        closeStart = System.nanoTime();
    }

    // ---------- helpers ----------
    private Feature first(String t) {
        for (Feature f : Features.ALL) if (f.category.equals(t)) return f;
        return null;
    }

    private List<Feature> current() {
        return Features.ALL.stream().filter(f -> f.category.equals(tab)).toList();
    }

    private float progress() {
        if (Features.MENU.mode("Animacao") == 3) return closing ? 0f : 1f;
        float dur = 260f / Math.max(0.1f, Features.MENU.get("Velocidade anim."));
        float t = ((System.nanoTime() - (closing ? closeStart : openStart)) / 1_000_000f) / dur;
        t = Math.max(0f, Math.min(1f, t));
        return closing ? 1f - t : t;
    }

    private static float ease(float t) { float u = 1f - t; return 1f - u * u * u; }
    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }

    private int x0() { return px + 154; }
    private int x1() { return px + W - 14; }
    private int tabX(int i) { return px + 90 + i * 70; }
    private int rowY(int i) { return py + 34 + i * 22; }
    private int settingY(int i) { return py + 82 + i * 28; }

    private void slide(Setting s, double mx) {
        float t = clamp01((float) ((mx - x0()) / (double) (x1() - x0())));
        s.value = s.min + t * (s.max - s.min);
    }

    // ---------- input ----------
    private void input(int mx, int my) {
        long h = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !prevDown;
        prevDown = down;
        if (!down) dragging = null;
        if (dragging != null) slide(dragging, mx);

        // captura de tecla
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) {
            boolean d = GLFW.glfwGetKey(h, k) == GLFW.GLFW_PRESS;
            if (listening != null && d && !prevKeys[k] && k != GLFW.GLFW_KEY_ESCAPE) {
                listening.key = (k == GLFW.GLFW_KEY_BACKSPACE) ? -1 : k;
                listening = null;
                Config.save();
            }
            prevKeys[k] = d;
        }

        if (!click || closing) return;

        // abas
        for (int i = 0; i < Features.TABS.length; i++) {
            if (mx >= tabX(i) - 4 && mx <= tabX(i) + 60 && my >= py + 4 && my <= py + 24) {
                tab = Features.TABS[i];
                selected = first(tab);
                listening = null;
                return;
            }
        }
        // lista de modulos
        List<Feature> fs = current();
        for (int i = 0; i < fs.size(); i++) {
            int y = rowY(i);
            if (mx >= px + 6 && mx <= px + 138 && my >= y - 1 && my <= y + 19) {
                Feature f = fs.get(i);
                if (f.toggleable && mx >= px + 104) { f.toggle(); Config.save(); }
                else { selected = f; listening = null; }
                return;
            }
        }
        if (selected == null) return;
        // botao de tecla
        if (mx >= x1() - 90 && mx <= x1() && my >= py + 50 && my <= py + 64) {
            listening = (listening == selected) ? null : selected;
            return;
        }
        // configuracoes
        for (int i = 0; i < selected.settings.size(); i++) {
            Setting s = selected.settings.get(i);
            int y = settingY(i);
            if (s.type == Setting.Type.MODE) {
                if (mx >= x1() - 110 && mx <= x1() && my >= y - 1 && my <= y + 13) {
                    s.value = (((int) s.value) + 1) % s.options.length;
                    return;
                }
            } else if (mx >= x0() - 4 && mx <= x1() + 4 && my >= y + 8 && my <= y + 20) {
                dragging = s;
                slide(s, mx);
                return;
            }
        }
    }

    // ---------- desenho ----------
    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);

        float p = progress();
        if (closing && p <= 0.001f) { super.close(); return; }
        float e = ease(p);
        int anim = Features.MENU.mode("Animacao");
        int oy = anim == 0 ? (int) (-(1f - e) * 40) : (anim == 1 ? (int) ((1f - e) * 40) : 0);
        px = (width - W) / 2;
        py = (height - H) / 2 + oy;

        input(mx, my);
        if (selected == null) selected = first(tab);

        int acc = Theme.accent();
        int style = Features.MENU.mode("Estilo");

        // painel
        c.fill(px, py, px + W, py + H, Theme.a(Theme.panel(), e));
        if (style == 2) {
            int bc = Theme.a(acc, e);
            c.fill(px, py, px + W, py + 2, bc);
            c.fill(px, py + H - 2, px + W, py + H, bc);
            c.fill(px, py, px + 2, py + H, bc);
            c.fill(px + W - 2, py, px + W, py + H, bc);
        }
        c.fill(px, py, px + W, py + 26, Theme.a(0xFF181824, e));
        c.fill(px, py + 26, px + W, py + 27, Theme.a(acc, e));
        c.drawText(textRenderer, "Visuals", px + 10, py + 9, Theme.a(0xFFFFFFFF, e), true);
        for (int i = 0; i < Features.TABS.length; i++) {
            boolean act = tab.equals(Features.TABS[i]);
            if (act) c.fill(tabX(i) - 4, py + 4, tabX(i) + 60, py + 24, Theme.a(0x30FFFFFF, e));
            c.drawText(textRenderer, Features.TABS[i], tabX(i) + 2, py + 9,
                    Theme.a(act ? acc : 0xFFAAAAAA, e), true);
        }
        c.fill(px + 144, py + 27, px + 145, py + H, Theme.a(0xFF2A2A3C, e));

        // lista de modulos (entra em cascata)
        List<Feature> fs = current();
        for (int i = 0; i < fs.size(); i++) {
            Feature f = fs.get(i);
            float rp = ease(clamp01((p * 1.4f - i * 0.08f) / 0.6f));
            int ox = (int) ((1f - rp) * -24);
            int y = rowY(i);
            if (f == selected) c.fill(px + 6 + ox, y - 1, px + 138 + ox, y + 19, Theme.a(0x40FFFFFF, rp));
            c.drawText(textRenderer, f.name, px + 12 + ox, y + 5, Theme.a(0xFFFFFFFF, rp), true);
            if (f.toggleable) {
                float v = toggleAnim.getOrDefault(f, f.enabled ? 1f : 0f);
                v += ((f.enabled ? 1f : 0f) - v) * 0.3f;
                toggleAnim.put(f, v);
                int tx = px + 108 + ox;
                c.fill(tx, y + 4, tx + 24, y + 14, Theme.a(v > 0.5f ? acc : 0xFF444455, rp));
                int kx = tx + 2 + (int) (v * 12);
                c.fill(kx, y + 5, kx + 8, y + 13, Theme.a(0xFFFFFFFF, rp));
            }
        }

        if (selected == null) return;

        // painel direito
        c.drawText(textRenderer, selected.name, x0(), py + 34, Theme.a(acc, e), true);
        String keyLabel = selected == Features.MENU ? "Tecla do menu" : "Tecla";
        c.drawText(textRenderer, keyLabel, x0(), py + 53, Theme.a(0xFFFFFFFF, e), false);
        c.fill(x1() - 90, py + 50, x1(), py + 64, Theme.a(0xFF2A2A3C, e));
        String kt = listening == selected ? "aperte uma tecla..." : Keys.name(selected.key);
        c.drawText(textRenderer, kt, x1() - 45 - textRenderer.getWidth(kt) / 2, py + 54,
                Theme.a(listening == selected ? acc : 0xFFFFFFFF, e), false);

        for (int i = 0; i < selected.settings.size(); i++) {
            Setting s = selected.settings.get(i);
            int y = settingY(i);
            s.shown += (s.value - s.shown) * 0.3f;
            if (s.type == Setting.Type.MODE) {
                c.drawText(textRenderer, s.name, x0(), y + 3, Theme.a(0xFFFFFFFF, e), false);
                c.fill(x1() - 110, y - 1, x1(), y + 13, Theme.a(0xFF2A2A3C, e));
                String ot = s.display();
                c.drawText(textRenderer, ot, x1() - 55 - textRenderer.getWidth(ot) / 2, y + 3,
                        Theme.a(0xFFFFFFFF, e), false);
            } else {
                c.drawText(textRenderer, s.name + ": " + s.display(), x0(), y, Theme.a(0xFFFFFFFF, e), false);
                int bx0 = x0(), bx1 = x1();
                float t = (s.shown - s.min) / (s.max - s.min);
                if (s.type == Setting.Type.HUE) {
                    int segs = 36, sw = Math.max(1, (bx1 - bx0) / segs);
                    for (int g = 0; g < segs; g++) {
                        int gx = bx0 + g * (bx1 - bx0) / segs;
                        c.fill(gx, y + 12, gx + sw + 1, y + 16,
                                Theme.a(0xFF000000 | Theme.hsb(g / (float) segs, 0.65f, 1f), e));
                    }
                } else {
                    c.fill(bx0, y + 12, bx1, y + 16, Theme.a(0xFF2A2A3C, e));
                    c.fill(bx0, y + 12, bx0 + (int) ((bx1 - bx0) * t), y + 16, Theme.a(acc, e));
                }
                int kx = bx0 + (int) ((bx1 - bx0) * t);
                c.fill(kx - 2, y + 10, kx + 2, y + 18, Theme.a(0xFFFFFFFF, e));
            }
        }
    }
}
