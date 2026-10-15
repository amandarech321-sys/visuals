package com.visuals;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Keys {
    public static boolean down(int key) {
        if (key < GLFW.GLFW_KEY_SPACE || key > GLFW.GLFW_KEY_LAST) return false;
        long h = MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(h, key) == GLFW.GLFW_PRESS;
    }

    public static String name(int key) {
        if (key < 0) return "Nenhuma";
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "Ctrl dir.";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "Ctrl esq.";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "Shift dir.";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "Shift esq.";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "Alt dir.";
            case GLFW.GLFW_KEY_LEFT_ALT: return "Alt esq.";
            case GLFW.GLFW_KEY_INSERT: return "Insert";
            case GLFW.GLFW_KEY_DELETE: return "Delete";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "Page Down";
            case GLFW.GLFW_KEY_SPACE: return "Espaco";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "Caps";
            default: break;
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        String n = GLFW.glfwGetKeyName(key, 0);
        return n != null ? n.toUpperCase() : "Tecla " + key;
    }
}
