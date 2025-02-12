package org.firstinspires.ftc.teamcode.auton.normal.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.auton.normal.runners.RightParkOnlyRunner;

@Autonomous(name="Right/ParkOnly [*    ]", group="Blue")
public class RightParkOnly extends LinearOpMode {
	@Override
	public void runOpMode() {
		new RightParkOnlyRunner().run(this);
	}
}
