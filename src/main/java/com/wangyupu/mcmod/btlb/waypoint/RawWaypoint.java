package com.wangyupu.mcmod.btlb.waypoint;

// ADT太好用了你们知道吗

public sealed interface RawWaypoint permits RawWaypoint.Nothing, RawWaypoint.Position, RawWaypoint.Chunk,
        RawWaypoint.Azimuth {
    record Nothing() implements RawWaypoint {
    }

    record Position(int x, int y, int z) implements RawWaypoint {
    }

    record Chunk(int x, int z) implements RawWaypoint {
    }

    record Azimuth(float angle) implements RawWaypoint {
    }

}
