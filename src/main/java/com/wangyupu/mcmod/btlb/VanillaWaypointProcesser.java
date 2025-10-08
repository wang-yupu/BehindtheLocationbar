package com.wangyupu.mcmod.btlb;

import com.wangyupu.mcmod.btlb.waypoint.RawWaypoint;
import net.minecraft.world.waypoint.TrackedWaypoint;

public class VanillaWaypointProcesser {
    public static RawWaypoint processTrackedWaypoint(TrackedWaypoint wp) {
        return switch (wp) {
            case TrackedWaypoint.Empty e -> new RawWaypoint.Nothing();

            case TrackedWaypoint.Positional p ->
                new RawWaypoint.Position(p.pos.getX(), p.pos.getY(), p.pos.getZ());

            case TrackedWaypoint.ChunkBased c ->
                new RawWaypoint.Chunk(c.chunkPos.x, c.chunkPos.z);

            case TrackedWaypoint.Azimuth a ->
                new RawWaypoint.Azimuth(a.azimuth);

            default -> new RawWaypoint.Nothing();
        };
    }
}
