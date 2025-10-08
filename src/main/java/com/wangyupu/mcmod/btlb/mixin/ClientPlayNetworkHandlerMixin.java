package com.wangyupu.mcmod.btlb.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WaypointS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.wangyupu.mcmod.btlb.VanillaWaypointProcesser;
import com.wangyupu.mcmod.btlb.WaypointDataProvider;
import com.wangyupu.mcmod.btlb.waypoint.RawWaypoint;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(at = @At("HEAD"), method = "onWaypoint")
	private void onWaypoint(WaypointS2CPacket packet, CallbackInfo info) {
		RawWaypoint rwp = VanillaWaypointProcesser.processTrackedWaypoint(packet.waypoint());
		WaypointDataProvider.getInstance().putRawData(packet.waypoint().getSource(),
				packet.waypoint().getConfig().color.orElse(0), rwp);
	}
}