package com.example.kbdisplace;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class KbDisplace implements ClientModInitializer {

	private static KeyMapping sideDisplace;
	private static KeyMapping upDisplace;
	private static KeyMapping pullDisplace;

	@Override
	public void onInitializeClient() {
		sideDisplace = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.kbdisplace.side",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"key.category.kbdisplace"
		));

		upDisplace = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.kbdisplace.up",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"key.category.kbdisplace"
		));

		pullDisplace = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.kbdisplace.pull",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"key.category.kbdisplace"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) return;

			while (sideDisplace.consumeClick()) {
				performSideDisplace(client);
			}
			while (upDisplace.consumeClick()) {
				performUpDisplace(client);
			}
			while (pullDisplace.consumeClick()) {
				performPullDisplace(client);
			}
		});
	}

	private void performSideDisplace(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) return;

		float originalYaw = player.getYRot();
		player.setYRot(originalYaw + 90.0f);
		player.yRotO = player.getYRot();
		player.setYHeadRot(player.getYRot());

		attackTarget(client, player);

		player.setYRot(originalYaw);
		player.yRotO = originalYaw;
		player.setYHeadRot(originalYaw);
	}

	private void performUpDisplace(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) return;

		float originalYaw = player.getYRot();
		player.setYRot(originalYaw + 180.0f);
		player.yRotO = player.getYRot();
		player.setYHeadRot(player.getYRot());

		attackTarget(client, player);

		player.setYRot(originalYaw);
		player.yRotO = originalYaw;
		player.setYHeadRot(originalYaw);
	}

	private void performPullDisplace(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) return;

		Entity target = findTarget(client, player, 3.5);
		if (target == null) return;

		float originalYaw = player.getYRot();
		float originalPitch = player.getXRot();

		Vec3 eyePos = player.getEyePosition(1.0f);
		Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
		Vec3 direction = targetPos.subtract(eyePos).normalize();

		float yaw = (float) (Math.toDegrees(Math.atan2(-direction.x, direction.z)));
		float pitch = (float) (Math.toDegrees(-Math.asin(direction.y)));

		player.setYRot(yaw);
		player.setXRot(pitch);
		player.yRotO = yaw;
		player.xRotO = pitch;
		player.setYHeadRot(yaw);

		attackTarget(client, player);

		player.setYRot(originalYaw);
		player.setXRot(originalPitch);
		player.yRotO = originalYaw;
		player.xRotO = originalPitch;
		player.setYHeadRot(originalYaw);
	}

	private void attackTarget(Minecraft client, LocalPlayer player) {
		Entity target = null;

		HitResult hit = client.hitResult;
		if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityHit = (EntityHitResult) hit;
			Entity e = entityHit.getEntity();
			if (e instanceof LivingEntity && e != player && e.isAlive()) {
				target = e;
			}
		}

		if (target == null) {
			target = findTarget(client, player, 3.5);
		}

		if (target != null && client.gameMode != null) {
			client.gameMode.attack(player, target);
			player.swing(InteractionHand.MAIN_HAND);
		}
	}

	private Entity findTarget(Minecraft client, LocalPlayer player, double range) {
		Vec3 eye = player.getEyePosition(1.0f);
		AABB box = player.getBoundingBox().inflate(range);
		List<Entity> entities = client.level.getEntities(player, box, e ->
				e instanceof LivingEntity && e.isAlive() && e != player && !(e instanceof Player && ((Player) e).isSpectator())
		);

		return entities.stream()
				.filter(e -> eye.distanceTo(e.position().add(0, e.getBbHeight() * 0.5, 0)) <= range)
				.min(Comparator.comparingDouble(e -> eye.distanceToSqr(e.position().add(0, e.getBbHeight() * 0.5, 0))))
				.orElse(null);
	}
      }
      
