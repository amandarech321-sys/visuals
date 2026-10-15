package com.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("visuals.properties"); }

    public static void load() {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file())) { p.load(in); } catch (IOException e) { return; }
        for (Feature f : Features.ALL) {
            String k = "f." + f.name + ".";
            String v = p.getProperty(k + "enabled");
            if (v != null && f.toggleable) f.enabled = Boolean.parseBoolean(v);
            v = p.getProperty(k + "key");
            if (v != null) try { f.key = Integer.parseInt(v); } catch (NumberFormatException ignored) {}
            for (Setting s : f.settings) {
                v = p.getProperty(k + "s." + s.name);
                if (v != null) try { s.value = Float.parseFloat(v); s.shown = s.value; } catch (NumberFormatException ignored) {}
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Feature f : Features.ALL) {
            String k = "f." + f.name + ".";
            p.setProperty(k + "enabled", Boolean.toString(f.enabled));
            p.setProperty(k + "key", Integer.toString(f.key));
            for (Setting s : f.settings) p.setProperty(k + "s." + s.name, Float.toString(s.value));
        }
        try (OutputStream out = Files.newOutputStream(file())) { p.store(out, "Visuals"); } catch (IOException ignored) {}
    }
}
