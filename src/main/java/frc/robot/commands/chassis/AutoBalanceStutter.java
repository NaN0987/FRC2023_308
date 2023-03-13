// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.chassis;

import edu.wpi.first.wpilibj.DriverStation;
//ShuffleBoard library
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.util.concurrent.TimeUnit;

//NAVX library
import com.kauailabs.navx.frc.AHRS;

//Subsystem
import frc.robot.subsystems.ChassisSubsystem;

//Constants
import frc.robot.Constants.ChassisConstants;


import edu.wpi.first.wpilibj2.command.CommandBase;

public class AutoBalanceStutter extends CommandBase {
    private final ChassisSubsystem m_drive;
    private AHRS NavX2;
    private final double kInitialPitchOffset;
    boolean autoBalanceXMode = false;

    public AutoBalanceStutter(ChassisSubsystem subsystem, AHRS Nav, double pitchOffset){
        this.m_drive = subsystem;
        this.NavX2 = Nav;
        this.kInitialPitchOffset = pitchOffset; 

        addRequirements(m_drive);
    }

    @Override
    public void initialize(){
        m_drive.setBrakeMode();
    }

    @Override
    public void execute() {
        double pitchAngleDegrees = NavX2.getYaw() - kInitialPitchOffset;

        if ( !autoBalanceXMode && 
            (Math.abs(pitchAngleDegrees) >= 
            Math.abs(ChassisConstants.kOffBalanceAngleThresholdDegrees))) {
            autoBalanceXMode = true;
        }
        else if ( autoBalanceXMode && 
                (Math.abs(pitchAngleDegrees) <= 
                Math.abs(ChassisConstants.kOnBalanceAngleThresholdDegrees))) {
            autoBalanceXMode = false;
        }
        
        // Control drive system automatically, 
        // driving in direction of pitch angle,
        // with a magnitude based upon the angle
        
        if ( autoBalanceXMode ) {
            double pitchAngleRadians = pitchAngleDegrees * (Math.PI / 180.0);
            double xAxisSpeed = Math.sin(pitchAngleRadians) ;
            
            //If we go too fast, the robot will go over the center of the pad and keep rocking back and forth.
            //If we go too slow, the robot will struggle to get over the charge pad since the ramp will make it slide downwards.
            //Brake mode SHOULD fix the latter issue, but it didn't seem to help that much.
            //We might want to consider using a cubic function instead of a sine function.
            xAxisSpeed *= ChassisConstants.kAutoBalanceMultiplier;
            
            m_drive.drive(xAxisSpeed, 0);
            try{
            TimeUnit.SECONDS.sleep(1);
            }
            catch(InterruptedException e){
                DriverStation.reportError("Error in AutoBalanceStutter: " + e.getMessage(), true);
            }
            m_drive.drive(0, 0);

            try{
                TimeUnit.SECONDS.sleep(1);
                }
                catch(InterruptedException e){
                    DriverStation.reportError("Error in AutoBalanceStutter: " + e.getMessage(), true);
                }

            SmartDashboard.putNumber("AutoBalanceSpeed", xAxisSpeed);
        }

        //If the robot is balanced, it should tell the motors to stop moving.
        else{
            m_drive.drive(0, 0);
        }
    }

    //This is for if DefaultDrive doesn't tell the motors to stop moving so the robot doesn't crash into a wall.
    @Override
    public void end(boolean interrupted){
        m_drive.drive(0, 0);
    }
}
