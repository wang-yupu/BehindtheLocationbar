package com.wangyupu.mcmod.btlb;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.datafixers.util.Either;

import com.wangyupu.mcmod.btlb.waypoint.EstimatedWaypoint;
import com.wangyupu.mcmod.btlb.waypoint.RawWaypoint;
import com.wangyupu.mcmod.btlb.waypoint.Waypoint;

public class WaypointDataProvider {

    private static WaypointDataProvider INSTANCE = new WaypointDataProvider();

    private WaypointDataProvider() {
        this.finalData = new HashMap<>();
        this.estimatingWaypoints = new HashMap<>();
    }

    public static WaypointDataProvider getInstance() {
        return INSTANCE;
    }

    private Map<Either<UUID, String>, EstimatedWaypoint> estimatingWaypoints;
    private Map<Either<UUID, String>, Waypoint> finalData;

    public void reset() {
        this.finalData.clear();
        this.estimatingWaypoints.clear();
    }

    public void putRawData(Either<UUID, String> identifier, int color, RawWaypoint wp) {
        if (!this.estimatingWaypoints.containsKey(identifier)) {
            this.estimatingWaypoints.put(identifier, new EstimatedWaypoint());
        }
        if (!this.finalData.containsKey(identifier)) {
            this.finalData.put(identifier, new Waypoint(identifier));
        }
        this.estimatingWaypoints.get(identifier).addInput(wp);
        this.finalData.get(identifier).color = color;
    }

    public void estimateAll() {
        for (Map.Entry<Either<UUID, String>, EstimatedWaypoint> entry : this.estimatingWaypoints.entrySet()) {
            if (!this.finalData.containsKey(entry.getKey())) {
                this.finalData.put(entry.getKey(), new Waypoint(entry.getKey()));
            }
            EstimatedWaypoint ewp = entry.getValue();
            Waypoint fwp = this.finalData.get(entry.getKey());

            ewp.estimate();
            fwp.status = ewp.getEstimateStatus();
            fwp.pos = ewp.getPosition();
        }
    }

    public Map<Either<UUID, String>, Waypoint> getFinalResult() {
        return this.finalData;
    }

}
