package com.visuals;

import java.util.ArrayList;
import java.util.List;

public class Feature {
    public final String name, category;
    public boolean enabled;
    public int key = -1;
    public boolean prevDown;
    public final boolean toggleable;
    public final List<Setting> settings = new ArrayList<>();

    public Feature(String name, String category, boolean enabled) {
        this.name = name; this.category = category; this.enabled = enabled;
        this.toggleable = !category.equals("Config");
    }

    public Feature add(Setting s) { settings.add(s); return this; }

    /** Adiciona modo de cor (fixa, arco-iris, pulsando) + matiz. */
    public Feature colors(float hue) {
        add(Setting.mode("Modo de cor", 0, "Fixa", "Arco-iris", "Pulsando"));
        add(Setting.hue("Matiz", hue));
        return this;
    }

    public float get(String n) {
        for (Setting s : settings) if (s.name.equals(n)) return s.value;
        return 0f;
    }

    public int mode(String n) { return (int) get(n); }

    public int color() {
        float hue = get("Matiz");
        long t = System.currentTimeMillis();
        int m = mode("Modo de cor");
        if (m == 1) hue = (t % 6000L) / 6000f;
        else if (m == 2) hue = ((hue + (float) Math.sin(t / 1000.0) * 0.08f) % 1f + 1f) % 1f;
        return Theme.hsb(hue, 0.65f, 1f);
    }

    public void toggle() {
        enabled = !enabled;
        HudRenderer.toast(name + (enabled ? " ligado" : " desligado"));
    }
}
