package org.firstinspires.ftc.teamcode.auton.odom.opmodes.left;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.auton.odom.runners.left.LeftSingleSpecimenRunner;


@Autonomous(name="Left [s+nn/a]", group="Blue")
public class LeftSingleSpecimen extends LinearOpMode {
	@Override
	public void runOpMode() {
		new LeftSingleSpecimenRunner().run(this);
	}
}