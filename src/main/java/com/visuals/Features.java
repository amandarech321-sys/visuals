package com.visuals;

import org.lwjgl.glfw.GLFW;

import java.util.List;

public class Features {
    public static final Feature WATERMARK = new Feature("Barra topo", "HUD", true);
    public static final Feature KEYSTROKES = new Feature("Keystrokes", "HUD", true);
    public static final Feature ARMOR = new Feature("Armor HUD", "HUD", true);
    public static final Feature TARGET_HUD = new Feature("TargetHUD", "HUD", true)
            .add(Setting.slider("Tempo", 1, 10, 4, "s"));
    public static final Feature TOGGLE_SPRINT = new Feature("Toggle Sprint", "HUD", false);
    public static final Feature DEATH_COORDS = new Feature("Death Coords", "HUD", true);

    public static final Feature HIT_EFFECT = new Feature("Hit Effect", "Efeitos", true)
            .add(Setting.mode("Estilo", 0, "Faiscas", "Coracoes", "Petalas", "Fogos"))
            .add(Setting.slider("Particulas", 4, 60, 24, ""))
            .colors(0.78f);
    public static final Feature TARGET_RING = new Feature("Anel no alvo", "Efeitos", true)
            .add(Setting.mode("Forma", 0, "Anel", "Espiral", "Coracoes", "Petalas"))
            .add(Setting.slider("Raio", 0.4f, 2f, 0.9f, "blocos"))
            .add(Setting.slider("Velocidade", 0.5f, 6f, 2.5f, "x"))
            .add(Setting.slider("Densidade", 1, 12, 5, ""))
            .colors(0.55f);
    public static final Feature TRAIL = new Feature("Rastro de fumaca", "Efeitos", false)
            .add(Setting.slider("Densidade", 1, 6, 2, ""));
    public static final Feature HIT_SOUND = new Feature("Hit Sound", "Efeitos", false)
            .add(Setting.mode("Som", 0, "Cristal", "Pop", "Flecha"))
            .add(Setting.slider("Volume", 0, 1, 0.7f, ""))
            .add(Setting.slider("Tom", 0.5f, 2f, 1.2f, ""));
    public static final Feature WORLD_FX = new Feature("World Effects", "Efeitos", false)
            .add(Setting.mode("Tipo", 0, "Petalas", "Neve", "Brilhos"))
            .add(Setting.slider("Densidade", 1, 10, 3, ""));

    public static final Feature MENU = new Feature("Menu", "Config", true)
            .add(Setting.hue("Cor do tema", 0.78f))
            .add(Setting.mode("Estilo", 0, "Vidro", "Escuro", "Neon"))
            .add(Setting.mode("Animacao", 0, "Deslizar cima", "Deslizar baixo", "Fade", "Desligada"))
            .add(Setting.slider("Velocidade anim.", 0.5f, 2f, 1f, "x"));

    static { MENU.key = GLFW.GLFW_KEY_RIGHT_CONTROL; }

    public static final String[] TABS = {"HUD", "Efeitos", "Config"};

    public static final List<Feature> ALL = List.of(
            WATERMARK, KEYSTROKES, ARMOR, TARGET_HUD, TOGGLE_SPRINT, DEATH_COORDS,
            HIT_EFFECT, TARGET_RING, TRAIL, HIT_SOUND, WORLD_FX, MENU);
}
