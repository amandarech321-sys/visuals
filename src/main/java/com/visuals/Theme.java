package com.visuals;

public class Theme {
    public static int hsb(float h, float s, float b) {
        return java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF;
    }

    public static int accent() {
        return 0xFF000000 | hsb(Features.MENU.get("Cor do tema"), 0.6f, 1f);
    }

    public static int panel() {
        int s = Features.MENU.mode("Estilo");
        return s == 0 ? 0xB0101018 : (s == 1 ? 0xF00C0C12 : 0xE00A0A14);
    }

    /** Multiplica o alpha de uma cor ARGB por k (0..1). */
    public static int a(int argb, float k) {
        k = Math.max(0f, Math.min(1f, k));
        int al = (int) (((argb >>> 24) & 0xFF) * k);
        return (al << 24) | (argb & 0xFFFFFF);
    }
}
