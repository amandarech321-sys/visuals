package com.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    private static float shownHp = 0;
    private static final int PANEL = 0xB0101018;

    private record Toast(String text, long time) {}
    private static final List<Toast> TOASTS = new ArrayList<>();

    public static void toast(String text) {
        TOASTS.add(new Toast(text, System.currentTimeMillis()));
        if (TOASTS.size() > 5) TOASTS.remove(0);
    }

    public static void render(DrawContext c, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        int w = c.getScaledWindowWidth(), h = c.getScaledWindowHeight();
        if (Features.WATERMARK.enabled) watermark(c, mc, w);
        if (Features.KEYSTROKES.enabled) keystrokes(c, mc, h);
        if (Features.ARMOR.enabled) armor(c, mc, w, h);
        if (Features.TARGET_HUD.enabled) targetHud(c, mc, w, h);
        toasts(c, mc);
    }

    private static void watermark(DrawContext c, MinecraftClient mc, int w) {
        String s = "Visuals  |  " + mc.getCurrentFps() + " fps  |  "
                + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        int tw = mc.textRenderer.getWidth(s);
        int x = (w - tw) / 2 - 8;
        c.fill(x, 4, x + tw + 16, 20, PANEL);
        c.fill(x, 4, x + tw + 16, 5, Theme.accent());
        c.drawText(mc.textRenderer, s, x + 8, 8, 0xFFFFFFFF, true);
    }

    private static void key(DrawContext c, MinecraftClient mc, String l, KeyBinding k, int x, int y) {
        boolean on = k.isPressed();
        c.fill(x, y, x + 22, y + 22, on ? Theme.a(Theme.accent(), 0.75f) : PANEL);
        c.drawText(mc.textRenderer, l, x + 11 - mc.textRenderer.getWidth(l) / 2, y + 7, 0xFFFFFFFF, true);
    }

    private static void keystrokes(DrawContext c, MinecraftClient mc, int h) {
        int x = 10, y = h - 80;
        key(c, mc, "W", mc.options.forwardKey, x + 25, y);
        key(c, mc, "A", mc.options.leftKey, x, y + 25);
        key(c, mc, "S", mc.options.backKey, x + 25, y + 25);
        key(c, mc, "D", mc.options.rightKey, x + 50, y + 25);
        if (Features.TOGGLE_SPRINT.enabled)
            c.drawText(mc.textRenderer, "Sprint: ligado", x, y + 52, 0xFFBBBBBB, true);
    }

    private static void armor(DrawContext c, MinecraftClient mc, int w, int h) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int x = w - 52, y = h / 2 - 40;
        for (EquipmentSlot s : slots) {
            ItemStack st = mc.player.getEquippedStack(s);
            c.fill(x - 2, y - 2, x + 46, y + 18, PANEL);
            if (!st.isEmpty()) {
                c.drawItem(st, x, y);
                if (st.isDamageable()) {
                    int pct = 100 * (st.getMaxDamage() - st.getDamage()) / st.getMaxDamage();
                    c.drawText(mc.textRenderer, pct + "%", x + 20, y + 4, pct > 30 ? 0xFFFFFFFF : 0xFFFF5555, true);
                }
            }
            y += 22;
        }
    }

    private static void targetHud(DrawContext c, MinecraftClient mc, int w, int h) {
        LivingEntity t = Effects.target;
        if (t == null || !t.isAlive()) return;
        if (System.currentTimeMillis() - Effects.targetTime > Features.TARGET_HUD.get("Tempo") * 1000) return;
        int x = w / 2 + 30, y = h / 2 + 20;
        int acc = Theme.accent();
        c.fill(x, y, x + 140, y + 38, PANEL);
        c.fill(x, y, x + 2, y + 38, acc);
        String name = t.getName().getString();
        c.fill(x + 6, y + 5, x + 34, y + 33, Theme.a(acc, 0.35f));
        String ini = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        c.drawText(mc.textRenderer, ini, x + 20 - mc.textRenderer.getWidth(ini) / 2, y + 15, 0xFFFFFFFF, true);
        c.drawText(mc.textRenderer, name, x + 40, y + 6, 0xFFFFFFFF, true);
        float max = Math.max(1f, t.getMaxHealth());
        shownHp += (t.getHealth() - shownHp) * 0.15f;
        float frac = Math.max(0f, Math.min(1f, shownHp / max));
        c.fill(x + 40, y + 20, x + 132, y + 26, 0xFF2A2A3C);
        c.fill(x + 40, y + 20, x + 40 + (int) (92 * frac), y + 26, 0xFF55DD77);
        c.drawText(mc.textRenderer, String.format(java.util.Locale.ROOT, "HP: %.1f", t.getHealth()),
                x + 40, y + 28, 0xFFBBBBBB, false);
    }

    private static void toasts(DrawContext c, MinecraftClient mc) {
        long now = System.currentTimeMillis();
        TOASTS.removeIf(t -> now - t.time() > 2500);
        int y = 30;
        for (Toast t : TOASTS) {
            long age = now - t.time();
            float k = age > 2100 ? (2500 - age) / 400f : Math.min(1f, age / 150f);
            int tw = mc.textRenderer.getWidth(t.text());
            c.fill(10, y, 10 + tw + 14, y + 18, Theme.a(PANEL, k));
            c.fill(10, y, 12, y + 18, Theme.a(Theme.accent(), k));
            c.drawText(mc.textRenderer, t.text(), 18, y + 5, Theme.a(0xFFFFFFFF, k), true);
            y += 22;
        }
    }
}
