package com.wangyupu.mcmod.btlb.waypoint;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;

import net.minecraft.util.math.Vec3i;

public class Waypoint {
    public enum WaypointStatus {
        POSITION,
        CHUNK,
        ESTIMATE,
        NO_WAY
    }

    public int color;
    public Either<UUID, String> identifier;
    @Nullable
    public Vec3i pos;
    public WaypointStatus status;

    public Waypoint(Either<UUID, String> identifier) {
        this.color = 0x000000;
        this.identifier = identifier;
        this.pos = null;
        this.status = WaypointStatus.ESTIMATE;
    }
}