
package org.firstinspires.ftc.teamcode.teleop.odom.runners;

import org.firstinspires.ftc.teamcode.robot.MergedRaptorRobot;

import lib8812.common.robot.IDriveableRobot;
import lib8812.common.robot.WheelPowers;
import lib8812.common.robot.poses.SimpleDegreePose;
import lib8812.common.teleop.ITeleOpRunner;
import lib8812.common.teleop.KeybindPattern;
import lib8812.common.teleop.TeleOpUtils;

public class MergedLocalizationTestRunner extends ITeleOpRunner {
    MergedRaptorRobot bot = new MergedRaptorRobot();
    private final WheelPowers wheelWeights = new WheelPowers(1, 1, 1, 1);
    boolean showExtraInfo = false;

    boolean LOCK_INTAKES = false;
    boolean LOCK_ARM = false;

    protected IDriveableRobot getBot() { return bot; }

    void testWheels() {
        double correctedRightY = TeleOpUtils.fineTuneInput(gamepad1.inner.right_stick_y);
        double correctedRightX = TeleOpUtils.fineTuneInput(gamepad1.inner.right_stick_x);
        double correctedLeftY = TeleOpUtils.fineTuneInput(gamepad1.inner.left_stick_y);
        double correctedLeftX = TeleOpUtils.fineTuneInput(gamepad1.inner.left_stick_x);

        WheelPowers correctedWheelPowers = new WheelPowers(
                (-correctedLeftY+correctedLeftX),
                (-correctedLeftY-correctedLeftX),
                (-correctedRightY-correctedRightX),
                (-correctedRightY+correctedRightX)
        );

        correctedWheelPowers.applyTo(bot, wheelWeights);
    }

    WheelPowers getRealWheelInputPowers() {
        return new WheelPowers(
                -gamepad1.inner.left_stick_x+gamepad1.inner.left_stick_x,
                -gamepad1.inner.left_stick_y-gamepad1.inner.left_stick_x,
                -gamepad1.inner.right_stick_y-gamepad1.inner.right_stick_x,
                -gamepad1.inner.right_stick_y+gamepad1.inner.right_stick_x
        );
    }

    void moveSpinningIntake() {
        double inputPwr = gamepad2.inner.right_trigger-gamepad2.inner.left_trigger;

        bot.intakeSmall.setPower(inputPwr); // we don't multiply by the ratio since the servo driver only accepts 1 a power value

        bot.intakeLarge.setPower(-inputPwr);
    }

    void moveLift() {
        bot.extensionLift.setPosition(
                bot.extensionLift.getTargetPosition()-(int) (gamepad2.inner.right_stick_y*50)
        );
    }

    void moveArm() {
        bot.arm.setPosition(
                bot.arm.getPosition()+ (int) (gamepad2.inner.left_stick_y*50)
        );
    }


    void moveClawRotate() {
        double change = 0;

        if (gamepad2.inner.left_bumper) change = -0.01;
        else if (gamepad2.inner.right_bumper) change = 0.01;

        bot.clawRotate.setPosition(
                Math.min(
                        Math.max(
                            bot.clawRotate.getPosition()+change, bot.CLAW_ROTATE_MIN_POS
                    ), bot.CLAW_ROTATE_MAX_POS
                )
        );
    }

    void moveActuator() {
        bot.actuator.setPower(
                gamepad1.inner.left_trigger-gamepad1.inner.right_trigger
        );
    }

    void limitLiftExtension() {
        double alphaDeg = bot.ARM_MAX_ROTATION_DEG*bot.arm.getPosition()/bot.arm.maxPos;
        double theta = Math.toRadians(alphaDeg < 90 ? alphaDeg : 180-alphaDeg);

        bot.extensionLift.maxPos = Math.min(
                (int) Math.floor(
                    (
                            (27/Math.cos(theta))-14
                    )*bot.LIFT_TICKS_PER_INCHES
                ), bot.LIFT_MAX_TICKS
        );
    }

    /* MACROS */

    void macroArmOut() {
        bot.arm.setPosition(bot.arm.maxPos);
    }

    void macroArmIn() {
        bot.arm.setPosition(bot.arm.minPos);
    }

    void macroSpecimenClutch(Runnable cancellationDelegate) {
        if (LOCK_INTAKES) return;

        LOCK_INTAKES = true;
        bot.intakeSmall.setPower(bot.INTAKE_SMALL_IN_DIRECTION);
        bot.intakeLarge.setPower(bot.INTAKE_LARGE_IN_DIRECTION);

        cancellationDelegate.run();
    }

    void macroHangSpecimen() {
        if (LOCK_ARM) return;

        LOCK_ARM = true;

        bot.arm.setPosition(bot.arm.getPosition()+100);

        macroSpecimenClutch(() -> {
            TeleOpUtils.setTimeout(() -> {
                bot.intakeSmall.setPower(0);
                bot.intakeLarge.setPower(0);

                LOCK_INTAKES = false;
                LOCK_ARM = false;
            }, 750); // 0.75 ms
        });
    }

    void macroBasketDropTop(int numInchingCycles) {
        if (numInchingCycles == 0) return;

        if (LOCK_INTAKES) return;

        LOCK_INTAKES = true;

        bot.intakeSmall.setPower(bot.INTAKE_SMALL_OUT_DIRECTION*0.2);
        bot.intakeLarge.setPower(bot.INTAKE_LARGE_OUT_DIRECTION*0.2);

        TeleOpUtils.setTimeout(() -> {
            bot.intakeSmall.setPower(0);
            bot.intakeLarge.setPower(0);

            LOCK_INTAKES = false;

            macroBasketDropTop(numInchingCycles-1);
        }, 500);
    }

    void macroBasketDropTop() { macroBasketDropTop(2); }

    void macroLiftFullExtension() { bot.extensionLift.setPosition(bot.extensionLift.maxPos); }

    void macroLiftFullRetract() { bot.extensionLift.setPosition(bot.extensionLift.minPos); }

    void macroLiftFullXXXToggle() {
        if (bot.extensionLift.getPosition() < 100) {
            macroLiftFullExtension();
            return;
        }

        macroLiftFullRetract();
    }

    void macroArmXXXToggle() {
        if (LOCK_ARM) return;

        LOCK_ARM = true;

        if (bot.arm.getPosition() < 200) {
            macroArmOut();
        }
        else macroArmIn();

        TeleOpUtils.setTimeout(() -> {
            bot.arm.waitForPosition();
            LOCK_ARM = false;
        }, 10);
    }

    /* END MACROS */

    protected void internalRun() {
        int counter = 0;

        KeybindPattern.GamepadBinder x = keybinder.bind("x");

        x.of(gamepad1).to(() -> showExtraInfo = !showExtraInfo);

        // hang bind release
        x.of(gamepad2).to(() -> {
            bot.arm.maxPos = bot.ARM_HANG_MAX_TICKS;
        });

        keybinder.bind("y").of(gamepad2).to(() -> LOCK_ARM = !LOCK_ARM);

        keybinder.bind("left_stick_button").of(gamepad2).to(this::macroArmXXXToggle);
        keybinder.bind("right_stick_button").of(gamepad2).to(this::macroLiftFullXXXToggle);
        keybinder.bind("a").of(gamepad2).to(this::macroHangSpecimen);

        long keyBinderLastMs = System.currentTimeMillis();

        while (opModeIsActive()) {
            long keyBinderDelta = System.currentTimeMillis()-keyBinderLastMs;

            testWheels();

            WheelPowers realWheelInputPowers = getRealWheelInputPowers();

            if (!LOCK_ARM) moveArm();
            limitLiftExtension();
            moveLift();
            moveClawRotate();
            moveActuator();
            if (!LOCK_INTAKES) moveSpinningIntake();

            if (keyBinderDelta > 100) {
                keybinder.executeActions();
                keyBinderLastMs = System.currentTimeMillis();
            }

            SimpleDegreePose otosPos = bot.otos.getPosition();

            telemetry.addData(
                    "otos", "x (%.4f) y (%.4f) heading (%.2f)",
                    otosPos.x, otosPos.y, otosPos.h
            );

            telemetry.addData(
                    "claw rotate", "pos (%.2f)",
                    bot.clawRotate.getPosition()
            );

            telemetry.addData(
                    "intake (small)", "power (%.2f)"+(LOCK_INTAKES ? " locked" : ""),
                    bot.intakeSmall.getPower()
            );

            telemetry.addData(
                    "intake (large)", "power (%.2f)"+(LOCK_INTAKES ? " locked" : ""),
                    bot.intakeLarge.getPower()
            );

            telemetry.addData(
                    "extension lift", "pos (%d), target (%d) power (%.2f) max pos (%d)",
                    bot.extensionLift.getPosition(), bot.extensionLift.getTargetPosition(),
                    bot.extensionLift.getPower(), bot.extensionLift.maxPos
            );

            telemetry.addData(
                    "arm", "pos (%d), target (%d), power (%.2f) max pos (%d)"+(LOCK_ARM ? " locked" : ""),
                    bot.arm.getPosition(), bot.arm.getTargetPosition(),
                    bot.arm.getPower(), bot.arm.maxPos
            );

            telemetry.addData(
                    "actuator", "power (%.2f)",
                    bot.actuator.getPower()
            );

            if (showExtraInfo) {
                telemetry.addData(
                        "Wheels (input)",
                        "leftFront (%.2f) leftBack (%.2f) rightFront (%.2f) rightBack (%.2f)",
                        realWheelInputPowers.leftFront,
                        realWheelInputPowers.leftBack,
                        realWheelInputPowers.rightFront,
                        realWheelInputPowers.rightBack
                );
                telemetry.addData(
                        "Wheels (corrected)",
                        "leftFront (%.2f) [tuned by %.2f] leftBack (%.2f) [tuned by %.2f] rightFront (%.2f) [tuned by %.2f] rightBack (%.2f) [tuned by %.2f]",
                        bot.leftFront.getPower(), realWheelInputPowers.leftFront - bot.leftFront.getPower(),
                        bot.leftBack.getPower(), realWheelInputPowers.leftBack - bot.leftBack.getPower(),
                        bot.rightFront.getPower(), realWheelInputPowers.rightFront - bot.rightFront.getPower(),
                        bot.rightBack.getPower(), realWheelInputPowers.rightBack - bot.rightBack.getPower()
                );
            }

            telemetry.addData("Verbosity Level", "%s", showExtraInfo ? "high" : "low");

            if (showExtraInfo) {
                for (String key : gamepad1.commonKeyList) {
                    telemetry.addData("Gamepad 1", "%s (%.2f)", key, gamepad1.getValue(key));
                }

                for (String key : gamepad2.commonKeyList) {
                    telemetry.addData("Gamepad 2", "%s (%.2f)", key, gamepad2.getValue(key));
                }
            }

            telemetry.update();

            counter++;
        }
    }
}