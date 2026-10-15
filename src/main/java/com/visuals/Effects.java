package com.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class Effects {
    public static LivingEntity target;
    public static long targetTime;
    private static boolean wasDead;

    public static void onAttack(Entity e) {
        if (!(e instanceof LivingEntity le)) return;
        target = le;
        targetTime = System.currentTimeMillis();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld w = mc.world;
        if (w == null) return;

        if (Features.HIT_EFFECT.enabled) {
            Feature f = Features.HIT_EFFECT;
            int n = (int) f.get("Particulas");
            int col = f.color();
            int style = f.mode("Estilo");
            double x = e.getX(), y = e.getY() + e.getHeight() * 0.6, z = e.getZ();
            for (int i = 0; i < n; i++) {
                double vx = (Math.random() - 0.5) * 0.5;
                double vy = Math.random() * 0.35;
                double vz = (Math.random() - 0.5) * 0.5;
                switch (style) {
                    case 1 -> { if (i % 3 == 0) w.addParticle(ParticleTypes.HEART, x, y, z, vx * 0.3, vy * 0.3, vz * 0.3); }
                    case 2 -> w.addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, vx * 0.5, vy * 0.2, vz * 0.5);
                    case 3 -> w.addParticle(ParticleTypes.FIREWORK, x, y, z, vx * 0.4, vy * 0.4, vz * 0.4);
                    default -> {
                        w.addParticle(ParticleTypes.END_ROD, x, y, z, vx, vy, vz);
                        if (i % 2 == 0) w.addParticle(new DustParticleEffect(col, 1.2f), x, y, z, vx, vy, vz);
                    }
                }
            }
        }

        if (Features.HIT_SOUND.enabled) {
            Feature f = Features.HIT_SOUND;
            SoundEvent s = switch (f.mode("Som")) {
                case 1 -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
                case 2 -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
                default -> SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
            };
            mc.getSoundManager().play(PositionedSoundInstance.master(s, f.get("Tom"), f.get("Volume")));
        }
    }

    public static void tick(MinecraftClient mc) {
        ClientWorld w = mc.world;
        if (w == null || mc.player == null) return;

        // Death Coords
        if (mc.player.isDead()) {
            if (!wasDead && Features.DEATH_COORDS.enabled) {
                wasDead = true;
                mc.player.sendMessage(Text.literal("Voce morreu em X: " + (int) mc.player.getX()
                        + " Y: " + (int) mc.player.getY() + " Z: " + (int) mc.player.getZ()), false);
            }
        } else wasDead = false;

        // Toggle Sprint (so pra frente)
        if (Features.TOGGLE_SPRINT.enabled && mc.currentScreen == null
                && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()
                && !mc.player.horizontalCollision && !mc.player.isUsingItem()
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }

        // Anel / formas girando em volta do ultimo alvo
        if (Features.TARGET_RING.enabled && target != null && target.isAlive()
                && System.currentTimeMillis() - targetTime < Features.TARGET_HUD.get("Tempo") * 1000) {
            Feature m = Features.TARGET_RING;
            double r = m.get("Raio");
            int n = (int) m.get("Densidade");
            int col = m.color();
            int shape = m.mode("Forma");
            double t = w.getTime() * 0.1 * m.get("Velocidade");
            double h = target.getHeight();
            for (int i = 0; i < n; i++) {
                double a = t + i * (Math.PI * 2 / n);
                double px = target.getX() + Math.cos(a) * r;
                double pz = target.getZ() + Math.sin(a) * r;
                switch (shape) {
                    case 1 -> w.addParticle(new DustParticleEffect(col, 0.8f), px,
                            target.getY() + h * (i / (double) n), pz, 0, 0, 0);
                    case 2 -> { if (w.getTime() % 4 == 0 && i % 2 == 0)
                            w.addParticle(ParticleTypes.HEART, px, target.getY() + h * 0.6, pz, 0, 0, 0); }
                    case 3 -> { if (w.getTime() % 3 == 0)
                            w.addParticle(ParticleTypes.CHERRY_LEAVES, px, target.getY() + h * 0.8, pz, 0, 0, 0); }
                    default -> {
                        double wave = (Math.sin(t * 0.5) + 1) / 2;
                        w.addParticle(new DustParticleEffect(col, 0.8f), px, target.getY() + h * wave, pz, 0, 0, 0);
                    }
                }
            }
        }

        // Rastro de fumaca
        if (Features.TRAIL.enabled && mc.player.getVelocity().horizontalLengthSquared() > 0.01) {
            int n = (int) Features.TRAIL.get("Densidade");
            for (int i = 0; i < n; i++) {
                w.addParticle(ParticleTypes.CLOUD,
                        mc.player.getX() + (Math.random() - 0.5) * 0.4, mc.player.getY() + 0.1,
                        mc.player.getZ() + (Math.random() - 0.5) * 0.4, 0, 0.01, 0);
            }
        }

        // World Effects
        if (Features.WORLD_FX.enabled) {
            int n = (int) Features.WORLD_FX.get("Densidade");
            ParticleEffect pe = switch (Features.WORLD_FX.mode("Tipo")) {
                case 1 -> ParticleTypes.SNOWFLAKE;
                case 2 -> ParticleTypes.END_ROD;
                default -> ParticleTypes.CHERRY_LEAVES;
            };
            for (int i = 0; i < n; i++) {
                w.addParticle(pe,
                        mc.player.getX() + (Math.random() - 0.5) * 16,
                        mc.player.getY() + 2 + Math.random() * 5,
                        mc.player.getZ() + (Math.random() - 0.5) * 16, 0, -0.02, 0);
            }
        }
    }
}
