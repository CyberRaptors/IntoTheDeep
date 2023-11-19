package org.firstinspires.ftc.teamcode.teleop.normal.runners;

import org.firstinspires.ftc.teamcode.robot.RaptorRobot;

import lib8812.common.teleop.IDriveableRobot;
import lib8812.common.teleop.ITeleopRunner;
import lib8812.common.teleop.TeleOpUtils;

/* RAPTOR TEST RUNNER

-- CONTROLS IMPLEMENTED IN THIS OPMODE --

Gamepad 1

    Right stick - right wheels (with fine tuning, strafe enabled)
    Left stick - left wheels (with fine tuning, strafe enabled)

    Right Trigger - linear actuator up
    Left Trigger - linear actuator down

    Dpad Down - Prep/Ready Plane
    Dpad Up - Shoot Plane

Gamepad 2

    Right Bumper - Open right claw
    Right Trigger - Close right claw
    Left Bumper - Open left claw
    Left Trigger - Close left claw

    Dpad Up - Rotate claw up
    Dpad Down - Rotate claw down

    Dpad Right - Rotate arm forwards
    Dpad Left - Rotate arm backwards
 */

public class RaptorTestRunner extends ITeleopRunner {
    RaptorRobot bot = new RaptorRobot();

    protected IDriveableRobot getBot() { return bot; };

    public double[] testWheelsAndReturnRealInputPower() {
        double correctedRightY = TeleOpUtils.fineTuneInput(gamepad1.right_stick_y, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedRightX = TeleOpUtils.fineTuneInput(gamepad1.right_stick_x, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedLeftY = TeleOpUtils.fineTuneInput(gamepad1.left_stick_y, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedLeftX = TeleOpUtils.fineTuneInput(gamepad1.left_stick_x, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);

        double correctedRightFront = -correctedRightY-correctedRightX;
        double correctedLeftFront = correctedLeftY-correctedLeftX;
        double correctedRightBack = -correctedRightY+correctedRightX;
        double correctedLeftBack = correctedLeftY+correctedLeftX;

        double realRightFront = -gamepad1.right_stick_y-gamepad1.right_stick_x;
        double realLeftFront = gamepad1.left_stick_y-gamepad1.left_stick_x;
        double realRightBack = -gamepad1.right_stick_y+gamepad1.right_stick_x;
        double realLeftBack = gamepad1.left_stick_y+gamepad1.left_stick_x;

        bot.rightFront.setPower(correctedRightFront);
        bot.leftFront.setPower(correctedLeftFront);
        bot.rightBack.setPower(correctedRightBack);
        bot.leftBack.setPower(correctedLeftBack);

        return new double[] { realLeftFront, realLeftBack, realRightFront, realRightBack };
    }

    public void testActuator() {
        bot.actuator.setPower(gamepad1.right_trigger-gamepad1.left_trigger);
    }

    public void testClaw() {
        if (gamepad2.right_bumper) {
            bot.clawOne.setPosition(bot.CLAW_ONE_OPEN);
        }

        if (gamepad2.left_bumper) {
            bot.clawTwo.setPosition(bot.CLAW_TWO_OPEN);
        }

        if (gamepad2.right_trigger > 0) {
            bot.clawOne.setPosition(bot.CLAW_CLOSED);
        }

        if (gamepad2.left_trigger > 0) {
            bot.clawTwo.setPosition(bot.CLAW_CLOSED);
        }
    }

    public void testClawRotate() {
        if (gamepad2.dpad_up) {
            bot.clawRotate.setPosition(
                    Math.min(bot.clawRotate.getPosition()+0.001, 1)
            );
        }
        if (gamepad2.dpad_down) {
            bot.clawRotate.setPosition(
                    Math.max(bot.clawRotate.getPosition()-0.001, 0)
            );
        }
    }

    public void testPlaneShooter() {
        if (gamepad1.dpad_up) {
            bot.planeShooter.setPosition(bot.PLANE_SHOT);
        }
        if (gamepad1.dpad_down) {
            bot.planeShooter.setPosition(bot.PLANE_READY);
        }
    }

    public void testArm() {
        if (gamepad1.dpad_right) {
            bot.arm.setPosition(bot.arm.getPosition()+1);
        }

        if (gamepad1.dpad_left) {
            bot.arm.setPosition(bot.arm.getPosition()-1);
        }
    }

    protected void internalRun() {
        int counter = 0;

        while (opModeIsActive()) {
            double[] realWheelInputPower = testWheelsAndReturnRealInputPower();

            testActuator();
            testClaw();
            testPlaneShooter();
            testArm();
            testClawRotate();

            telemetry.addData(
                    "Wheels (input)",
                    "leftFront (%.2f) leftBack (%.2f) rightFront (%.2f) rightBack (%.2f)",
                    realWheelInputPower[0],
                    realWheelInputPower[1],
                    realWheelInputPower[2],
                   realWheelInputPower[3]
            );
            telemetry.addData(
                    "Wheels (corrected)",
                    "leftFront (%.2f) [tuned by %.2f] leftBack (%.2f) [tuned by %.2f] rightFront (%.2f) [tuned by %.2f] rightBack (%.2f) [tuned by %.2f]",
                    bot.leftFront.getPower(), realWheelInputPower[0]-bot.leftFront.getPower(),
                    bot.leftBack.getPower(), realWheelInputPower[1]-bot.leftBack.getPower(),
                    bot.rightFront.getPower(), realWheelInputPower[2]-bot.rightFront.getPower(),
                    bot.rightBack.getPower(), realWheelInputPower[3]-bot.rightBack.getPower()
            );
            telemetry.addData("Plane Launcher", bot.planeShooter.getPosition() < bot.PLANE_READY ? "SHOT" : "READY");
            telemetry.addData("Claw", "one[%s] two[%s]", (bot.clawOne.getPosition() == bot.CLAW_ONE_OPEN ? "OPEN" : "CLOSED"), (bot.clawTwo.getPosition() == bot.CLAW_ONE_OPEN ? "OPEN" : "CLOSED"));
            telemetry.addData("Actuator", "power (%.2f)", bot.actuator.getPower());
            telemetry.addData("Claw Rotate Servo", "pos (%.2f)", bot.clawRotate.getPosition());
            telemetry.addData("Arm", "pos (%.2f)", bot.arm.getPosition());
            telemetry.update();

            counter++;
        }
    }
}