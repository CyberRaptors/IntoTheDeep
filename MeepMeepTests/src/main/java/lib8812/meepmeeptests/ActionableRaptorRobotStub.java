package lib8812.meepmeeptests;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import org.jetbrains.annotations.NotNull;

public class ActionableRaptorRobotStub {
    static class EmptyAction implements Action {
        @Override
        public boolean run(@NotNull TelemetryPacket telemetryPacket) {
            return false;
        }
    }

    public Action setArmPos(int pos) {
        return new EmptyAction();
    }

    public Action setMaxArmPos() {
        return new EmptyAction();
    }

    public Action setExtensionLiftPos(int pos) {
        return new EmptyAction();
    }

    public Action setClawRotatePos(double pos) {
        return new EmptyAction();
    }

    public Action standardFrog() {
        return new EmptyAction();
    }

    public Action prepareArmForBackDrop() {
        return new EmptyAction();
    }

    public Action spitSample() {
        return new EmptyAction();
    }

    public Action ascend() {
        return new EmptyAction();
    }

    public Action clutchPreload() {
        return new EmptyAction();
    }

    public Action hangPreloadStationary() {
        return new EmptyAction();
    }

    public Action prepareArmForSpecimenHang() {
        return new EmptyAction();
    }

    public Action retractArm() {
        return new EmptyAction();
    }
}
