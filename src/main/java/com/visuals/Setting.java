package com.visuals;

import java.util.Locale;

public class Setting {
    public enum Type { SLIDER, MODE, HUE }

    public final String name;
    public final Type type;
    public final float min, max;
    public final String[] options;
    public final String unit;
    public float value;
    public float shown; // valor suavizado, so pra animacao do menu

    private Setting(String name, Type type, float min, float max, float def, String unit, String[] options) {
        this.name = name; this.type = type; this.min = min; this.max = max;
        this.value = def; this.shown = def; this.unit = unit; this.options = options;
    }

    public static Setting slider(String n, float min, float max, float def, String unit) {
        return new Setting(n, Type.SLIDER, min, max, def, unit, null);
    }

    public static Setting mode(String n, int def, String... opts) {
        return new Setting(n, Type.MODE, 0, opts.length - 1, def, "", opts);
    }

    public static Setting hue(String n, float def) {
        return new Setting(n, Type.HUE, 0, 1, def, "", null);
    }

    public String display() {
        if (type == Type.MODE) return options[Math.max(0, Math.min(options.length - 1, (int) value))];
        if (type == Type.HUE) return String.format(Locale.ROOT, "%.0f", value * 360) + "\u00B0";
        String f = (max - min) >= 10 ? "%.0f" : "%.2f";
        return String.format(Locale.ROOT, f, value) + (unit.isEmpty() ? "" : " " + unit);
    }
}
