package com.wangyupu.mcmod.btlb.waypoint;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.wangyupu.mcmod.btlb.BehindTheLocationBar;
import com.wangyupu.mcmod.btlb.waypoint.Waypoint.WaypointStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.Pair;

enum EstimateStatus {
    NO,
    POS,
    CHUNK,
    AZIMUTH,
}

class PointLocalization {

    // 将传感器角（以 +y 为 0，逆时针为正）转换为数学角（以 +x 为 0，逆时针为正）
    private static double toMathAngle(double O) {
        return Math.PI / 2.0 + O;
    }

    /**
     * 根据两点坐标与观测角度计算目标点A坐标（O 是传感器给的弧度，定义：+y 为0，逆时针为正）
     */
    public static double[] computeA(double x1, double y1, double O1,
            double x2, double y2, double O2) {

        // 1) 角度换到数学角
        double t1a = toMathAngle(O1);
        double t2a = toMathAngle(O2);

        // 2) 正确的分母：sin(theta1 - theta2)
        double den = Math.sin(t1a - t2a);
        if (Math.abs(den) < 1e-9) {
            // 近乎平行，数值不稳定
            return new double[] { Double.NaN, Double.NaN };
        }

        // 3) 正确的 t1 分子（注意符号）
        double num = (y2 - y1) * Math.cos(t2a) - (x2 - x1) * Math.sin(t2a);
        double t1 = num / den;

        // 4) 回代得到 A
        double xA = x1 + t1 * Math.cos(t1a);
        double yA = y1 + t1 * Math.sin(t1a);

        return new double[] { xA, yA };
    }

}

public class EstimatedWaypoint {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private Vec3i estimateResult;
    private EstimateStatus estimateStatus;

    private ChunkPos chunkEsimatedInput;
    private List<Pair<Vec3i, Float>> azimuthEsimateInputs;

    public EstimatedWaypoint() {
        this.resetInputs();
    }

    public void resetInputs() {
        this.estimateResult = null;
        this.estimateStatus = EstimateStatus.NO;
        this.chunkEsimatedInput = null;
        this.azimuthEsimateInputs = null;
        BehindTheLocationBar.LOGGER.info("Resetted all inputs");
    }

    public void addInput(RawWaypoint rd) {
        switch (rd) {
            case RawWaypoint.Nothing n -> {
                this.resetInputs();
                this.estimateStatus = EstimateStatus.NO;
                break;
            }

            case RawWaypoint.Position p -> {
                if (this.estimateStatus != EstimateStatus.POS) {
                    this.resetInputs();
                    this.estimateStatus = EstimateStatus.POS;
                }
                this.estimateResult = new Vec3i(p.x(), p.y(), p.z());
                break;
            }

            case RawWaypoint.Chunk c -> {
                if (this.estimateStatus != EstimateStatus.CHUNK) {
                    this.resetInputs();
                    this.estimateStatus = EstimateStatus.CHUNK;
                }
                this.chunkEsimatedInput = new ChunkPos(c.x(), c.z());
                break;
            }

            case RawWaypoint.Azimuth a -> {
                if (this.estimateStatus != EstimateStatus.AZIMUTH) {
                    this.resetInputs();
                    this.estimateStatus = EstimateStatus.AZIMUTH;
                    this.azimuthEsimateInputs = new ArrayList<>();
                }
                if (client.player != null) {
                    float delta = 0.0f;
                    if (this.azimuthEsimateInputs.size() != 0) {
                        delta = Math.abs(this.azimuthEsimateInputs.getLast().getRight() - a.angle());
                    }
                    if (this.azimuthEsimateInputs.size() == 0 || delta > 0.06) {
                        Vec3d playerPos = client.player.getPos();
                        this.azimuthEsimateInputs
                                .add(new Pair<Vec3i, Float>(new Vec3i((int) playerPos.x, (int) playerPos.y,
                                        (int) playerPos.z), a.angle()));
                        BehindTheLocationBar.LOGGER.warn("[azimuth esimate] delta: " + delta + " || size == 0 => add");
                        BehindTheLocationBar.LOGGER.info(
                                String.format("[azimuth esimate]: "
                                        + this.azimuthEsimateInputs.getLast().getLeft().toShortString() + " @ "
                                        + this.azimuthEsimateInputs
                                                .getLast().getRight()));
                        while (this.azimuthEsimateInputs.size() > 2) {
                            this.azimuthEsimateInputs.remove(0);
                        }
                    }
                    BehindTheLocationBar.LOGGER.warn(String.format("[azimuth esimate] dropped (%f)", delta));
                }

                break;
            }
        }
        ;
    }

    public void estimate() {
        switch (this.estimateStatus) {
            case EstimateStatus.NO:
                this.estimateResult = null;
                break;

            case EstimateStatus.POS:
                break;

            case EstimateStatus.CHUNK:
                this.estimateResult = new Vec3i(this.chunkEsimatedInput.x * 16 + 8, 0,
                        this.chunkEsimatedInput.z * 16 + 8);
                break;

            case EstimateStatus.AZIMUTH:
                if (this.azimuthEsimateInputs.size() < 2) {
                    this.estimateResult = new Vec3i(0, 0, 0);
                } else {
                    int x = 0, z = 0;
                    Pair<Vec3i, Float> p1 = this.azimuthEsimateInputs.get(0);
                    Pair<Vec3i, Float> p2 = this.azimuthEsimateInputs.get(1);
                    double[] r = PointLocalization.computeA(p1.getLeft().getX(), p1.getLeft().getZ(), p1.getRight(),
                            p2.getLeft().getX(), p2.getLeft().getZ(), p2.getRight());
                    x = (int) r[0];
                    z = (int) r[1];
                    this.estimateResult = new Vec3i(x, 0, z);
                }

                break;

            default:
                this.estimateResult = null;
                break;
        }
    }

    @Nullable
    public Vec3i getPosition() {
        return this.estimateResult;
    }

    public WaypointStatus getEstimateStatus() {
        return switch (this.estimateStatus) {
            case EstimateStatus.NO -> WaypointStatus.NO_WAY;
            case EstimateStatus.POS -> WaypointStatus.POSITION;
            case EstimateStatus.CHUNK -> WaypointStatus.CHUNK;
            case EstimateStatus.AZIMUTH -> WaypointStatus.ESTIMATE;
        };
    }
}
