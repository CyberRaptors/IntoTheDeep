package org.firstinspires.ftc.teamcode.teleop.normal.runners;

import org.firstinspires.ftc.teamcode.robot.RaptorRobot;

import java.util.concurrent.TimeUnit;

import lib8812.common.robot.IDriveableRobot;
import lib8812.common.robot.WheelPowers;
import lib8812.common.teleop.ITeleopRunner;
import lib8812.common.teleop.TeleOpUtils;

/* RAPTOR TEST RUNNER

-- THE FOLLOWING CONTROLS ARE IMPLEMENTED IN THIS OPMODE --
-- ALL OF THE CONTROLS IMPLEMENTED IN THIS OPMODE ARE EXPERIMENTAL AND NEED EMPIRICAL TESTING --

Gamepad 1

    Right stick - right wheels (with fine tuning, strafe enabled)
    Left stick - left wheels (with fine tuning, strafe enabled)

    Right Trigger - linear actuator up
    Left Trigger - linear actuator down

    Dpad Down - Prep/Ready Plane
    Dpad Up - Shoot Plane

    X Button - Changes verbosity level of info shown in telemetry

Gamepad 2

    Right Bumper - Open right claw
    Right Trigger - Close right claw
    Left Bumper - Open left claw
    Left Trigger - Close left claw

    Right Stick (Y) - Rotate arm forwards/backwards

    Left Stick (Y) - Rotate claw up/down

    X Button - Initializes endgame final sequence
    |   X Button - Used to confirm endgame final sequence
    |   B Button - Cancel sequence

    Dpad Down [future/unstable] - Runs a pickup sequence which uses the spinning intake to feed pixels into both claws

    -- THE FOLLOWING MACROS ARE EXTREMELY VOLATILE
        (they utilize more controls of the already-in-use joysticks,
        creating a greater chance for mistakes) --

    Right Stick (X) - When pushed (>0.7) in either direction, readies arm/claw in backdrop placing position
    Left Stick (X) -> Right - When pushed (>0.7), closes both claws
    Left Stick (X) -> Left - When pushed (>0.7), opens both claws

    Left Stick Button - Readies arm/claw in pickup position
    Right Stick Button - Rests arm/claw inside of robot

 */

public class RaptorTestRunner extends ITeleopRunner {
    final double MACRO_COMMAND_SAFE_JOYSTICK_THRESH = 0.7;
    final WheelPowers wheelWeights = new WheelPowers(0.67, 0.67, 0.65, 1);

    RaptorRobot bot = new RaptorRobot();
    boolean showExtraInfo = false;
    boolean ACTUATOR_LOCKED = false;

    protected IDriveableRobot getBot() { return bot; }

    public void testWheels() {
        double correctedRightY = TeleOpUtils.fineTuneInput(gamepad1.inner.right_stick_y, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedRightX = TeleOpUtils.fineTuneInput(gamepad1.inner.right_stick_x, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedLeftY = TeleOpUtils.fineTuneInput(gamepad1.inner.left_stick_y, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);
        double correctedLeftX = TeleOpUtils.fineTuneInput(gamepad1.inner.left_stick_x, TeleOpUtils.DEFAULT_FINE_TUNE_THRESH);

        double correctedRightFront = (-correctedRightY-correctedRightX)*wheelWeights.rightFront;
        double correctedLeftFront = (correctedLeftY-correctedLeftX)*wheelWeights.leftFront;
        double correctedRightBack = (-correctedRightY+correctedRightX)*wheelWeights.rightBack;
        double correctedLeftBack = (correctedLeftY+correctedLeftX)*wheelWeights.leftBack;

        bot.rightFront.setPower(correctedRightFront);
        bot.leftFront.setPower(correctedLeftFront);
        bot.rightBack.setPower(correctedRightBack);
        bot.leftBack.setPower(correctedLeftBack);
    }

    public WheelPowers getRealWheelInputPowers() {
        return new WheelPowers(
                -gamepad1.inner.right_stick_y-gamepad1.inner.right_stick_x,
                gamepad1.inner.left_stick_y-gamepad1.inner.left_stick_x,
                -gamepad1.inner.right_stick_y+gamepad1.inner.right_stick_x,
                gamepad1.inner.left_stick_y+gamepad1.inner.left_stick_x
        );
    }

    public void testActuator() {
        if (!ACTUATOR_LOCKED) bot.actuator.setPower(gamepad1.inner.right_trigger-gamepad1.inner.left_trigger);
    }

    public void testClaw() {
        gamepad2.map("right_bumper").to(
                () -> bot.clawOne.setLabeledPosition("OPEN")
        ).and("left_bumper").to(
                () -> bot.clawTwo.setLabeledPosition("OPEN")
        ).and("right_trigger").to(
                () -> bot.clawOne.setLabeledPosition("CLOSED")
        ).and("left_trigger").to(
                () -> bot.clawTwo.setLabeledPosition("CLOSED")
        );
    }

    public void testClawRotate() {
        bot.clawRotate.setPosition(
                Math.max(
                        Math.min(
                            bot.clawRotate.getPosition()+
                               (gamepad2.inner.left_stick_y/1000)
                            , 1
                    ), 0
                )
        );
    }

    public void testPlaneShooter() {
        gamepad1.map("dpad_up").to(
                () -> bot.planeShooter.setLabeledPosition("SHOT")
        ).and("dpad_down").to(
                () -> bot.planeShooter.setLabeledPosition("READY")
        );
    }

    public void testArm() {
        bot.arm.setPosition(
                bot.arm.getPosition()+(int) gamepad2.inner.right_stick_y*2
        );

//        bot.arm.setPower(gamepad2.inner.right_stick_y);
    }

    public void endgameSequence() {
        gamepad2.map("x").to(() -> {
            telemetry.addLine("You have initialized the final endgame sequence. Make sure that you are in the correct launching spot. Press X again to confirm or B to cancel. You may not use any other controls until the sequence is confirmed or canceled.");
            telemetry.update();

            while (gamepad2.inner.x); // wait for user to release x first

            while (!(gamepad2.inner.x || gamepad2.inner.b));

            if (gamepad2.inner.b) return;

            if (ACTUATOR_LOCKED) {
                telemetry.addLine("Could not run sequence; the actuator is currently being held by another sequence.");
                telemetry.update();

                return;
            }

            ACTUATOR_LOCKED = true; // make sure no one else sets power to the actuator since it will be running async (see below setTimeout call)

            bot.actuator.setPower(1);

            // shoot plane twice for good measure
            bot.planeShooter.setLabeledPosition("READY");
            bot.planeShooter.setLabeledPosition("SHOT");

            bot.planeShooter.setLabeledPosition("READY");
            bot.planeShooter.setLabeledPosition("SHOT");

            setTimeout(() -> {
                bot.actuator.setPower(0);
                ACTUATOR_LOCKED = false;
            }, 5000);

            while (gamepad2.inner.x);
        });
    }

    public void armSequence_restingPosition() {
//        bot.clawRotate.setPosition(0.3);
        bot.arm.setPosition(bot.arm.minPos);
    }

    public void armSequence_backdropPlacePosition() {
//        bot.arm.setPosition(bot.arm.maxPos*3/4); // 3/4 of the full span
        bot.clawRotate.setPosition(0);
    }

    public void armSequences() {
        boolean commandArmUp = (gamepad2.inner.right_stick_x > MACRO_COMMAND_SAFE_JOYSTICK_THRESH) || (gamepad2.inner.right_stick_x < -MACRO_COMMAND_SAFE_JOYSTICK_THRESH);

        gamepad2.map("left_stick_button").to(() -> {
            setTimeout(() -> {
                try {
                    bot.clawOne.setLabeledPosition("CLOSED");
                    bot.clawTwo.setLabeledPosition("CLOSED");
                    TimeUnit.MILLISECONDS.sleep(300);
                    bot.clawRotate.setPosition(0.735);
                    TimeUnit.MILLISECONDS.sleep(500);
                    bot.arm.setPosition(bot.arm.maxPos - 100);
                    bot.arm.waitForPosition();
                    TimeUnit.MILLISECONDS.sleep(300);
                    bot.arm.setPosition(bot.arm.maxPos);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }, 0);

            sleep(100);
        }).and("right_stick_button").to(
                () -> setTimeout(this::armSequence_restingPosition, 0)
        ).and(commandArmUp).to(
                () -> setTimeout(this::armSequence_backdropPlacePosition, 0)
        );
    }

    public void clawSequence_openBoth() {
        bot.clawOne.setLabeledPosition("OPEN");
        bot.clawTwo.setLabeledPosition("OPEN");
    }

    public void clawSequence_closeBoth() {
        bot.clawOne.setLabeledPosition("CLOSED");
        bot.clawTwo.setLabeledPosition("CLOSED");
    }

    public void clawSequences() {
        boolean commandCloseClaws = gamepad2.inner.left_stick_x > MACRO_COMMAND_SAFE_JOYSTICK_THRESH;
        boolean commandOpenClaws = gamepad2.inner.left_stick_x < -MACRO_COMMAND_SAFE_JOYSTICK_THRESH;

        gamepad2.map(commandCloseClaws).to(
                this::clawSequence_closeBoth
        ).and(commandOpenClaws).to(
                this::clawSequence_openBoth
        );
    }

    public void FUTURE_pickupSequence() {
//        gamepad2.map("dpad_down").to(() -> setTimeout(() -> {
//            armSequence_restingPosition();
//            clawSequence_openBoth();
//
//            sleep(300);
//
//            clawSequence_closeBoth();
//
//            armSequence_backdropPlacePosition(); // may need to not use backdrop place position and instead place arm in a shorter position in order to move under the rigging
//        }, 0));
    }

    public void testIntakes()
    {
        bot.spinTwo.setPower(gamepad2.inner.right_stick_y);
        bot.spinOne.setPower(gamepad2.inner.right_stick_y);
    }

    protected void internalRun() {
        int counter = 0;

        while (opModeIsActive()) {
            testWheels();
            testActuator();
            testClaw();
            testPlaneShooter();
            testArm();
//            testIntakes();
            testClawRotate();
            endgameSequence();
            armSequences();
            clawSequences();
            FUTURE_pickupSequence();

            WheelPowers realWheelInputPowers = getRealWheelInputPowers();

            if (gamepad1.inner.x) showExtraInfo = !showExtraInfo; // telemetry verbosity

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

            telemetry.addData("Plane Launcher", bot.planeShooter.getPositionLabel());
            telemetry.addData("Claw", "one (%s) two (%s)", bot.clawOne.getPositionLabel(), bot.clawTwo.getPositionLabel());
            telemetry.addData("Actuator", "power (%.2f)%s", bot.actuator.getPower(), ACTUATOR_LOCKED ? " (locked by a sequence)" : "");
            telemetry.addData("Claw Rotate Servo", "pos (%.4f)", bot.clawRotate.getPosition());
            telemetry.addData("Arm", "power (%d)", bot.arm.getPosition());
            telemetry.addData("Verbose", showExtraInfo);

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