package com.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public class VisualsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Feature menu = Features.MENU;
            boolean menuDown = Keys.down(menu.key);
            boolean menuEdge = menuDown && !menu.prevDown;
            menu.prevDown = menuDown;
            if (menuEdge) {
                if (mc.currentScreen instanceof ClickGuiScreen g) {
                    if (!g.isListening()) g.close();
                } else if (mc.currentScreen == null) {
                    mc.setScreen(new ClickGuiScreen());
                }
            }

            // tecla propria de cada modulo (so com o jogo em foco, sem menu/chat aberto)
            for (Feature f : Features.ALL) {
                if (f == menu || !f.toggleable) continue;
                boolean d = Keys.down(f.key);
                if (d && !f.prevDown && mc.currentScreen == null) { f.toggle(); }
                f.prevDown = d;
            }

            Effects.tick(mc);
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> HudRenderer.render(ctx, MinecraftClient.getInstance()));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) Effects.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}
