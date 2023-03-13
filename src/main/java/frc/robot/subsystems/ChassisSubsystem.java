package frc.robot.subsystems;

//Motor libraries
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

//Constants
import frc.robot.Constants.ChassisConstants;

//Drive train object
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ChassisSubsystem extends SubsystemBase {
    
    private final CANSparkMax m_leftFront = new CANSparkMax(ChassisConstants.kLeftFrontPort, MotorType.kBrushless);
    private final CANSparkMax m_leftRear = new CANSparkMax(ChassisConstants.kLeftRearPort, MotorType.kBrushless);
    MotorControllerGroup m_left = new MotorControllerGroup(m_leftFront, m_leftRear);

    private final CANSparkMax m_rightFront = new CANSparkMax(ChassisConstants.kRightFrontPort, MotorType.kBrushless);
    private final CANSparkMax m_rightRear = new CANSparkMax(ChassisConstants.kRightRearPort, MotorType.kBrushless);
    MotorControllerGroup m_right = new MotorControllerGroup(m_rightFront, m_rightRear);

    //Get Encoders
    private RelativeEncoder m_leftFrontEncoder = m_leftFront.getEncoder();
    private RelativeEncoder m_rightFrontEncoder = m_rightFront.getEncoder();
    private RelativeEncoder m_rightRearEncoder = m_rightRear.getEncoder();
    private RelativeEncoder m_leftRearEncoder = m_leftRear.getEncoder();

    private final DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);


    public ChassisSubsystem(){
        m_leftFront.restoreFactoryDefaults();
        m_rightFront.restoreFactoryDefaults();
        m_rightRear.restoreFactoryDefaults();
        m_leftRear.restoreFactoryDefaults();

        m_leftFront.setSmartCurrentLimit(ChassisConstants.kCurrentLimit);
        m_rightFront.setSmartCurrentLimit(ChassisConstants.kCurrentLimit);
        m_rightRear.setSmartCurrentLimit(ChassisConstants.kCurrentLimit);
        m_leftRear.setSmartCurrentLimit(ChassisConstants.kCurrentLimit);
        
        /*m_leftFront.setIdleMode(IdleMode.kCoast);
        m_rightFront.setIdleMode(IdleMode.kCoast);
        m_rightRear.setIdleMode(IdleMode.kCoast);
        m_leftRear.setIdleMode(IdleMode.kCoast);*/
        
        m_leftFront.setIdleMode(IdleMode.kBrake);
        m_rightFront.setIdleMode(IdleMode.kBrake);
        m_rightRear.setIdleMode(IdleMode.kBrake);
        m_leftRear.setIdleMode(IdleMode.kBrake);
        
        //Maybe make constants for this?
        m_leftFront.setInverted(false);
        m_leftRear.setInverted(false);
        m_rightFront.setInverted(true);
        m_rightRear.setInverted(true);

        resetEncoders();
    }

    public void setBrakeMode(){
        m_leftFront.setIdleMode(IdleMode.kBrake);
        m_rightFront.setIdleMode(IdleMode.kBrake);
        m_rightRear.setIdleMode(IdleMode.kBrake);
        m_leftRear.setIdleMode(IdleMode.kBrake);
    }

    public void setCoastMode(){
        m_leftFront.setIdleMode(IdleMode.kCoast);
        m_rightFront.setIdleMode(IdleMode.kCoast);
        m_rightRear.setIdleMode(IdleMode.kCoast);
        m_leftRear.setIdleMode(IdleMode.kCoast);
    }

    public void drive(double xSpeed, double zRotation){

        //Safety mode
        /*if(xSpeed>0.5){
            xSpeed = 0.5;
        }
        if(xSpeed<-0.5){
            xSpeed = -0.5;
        }
        if(zRotation>0.5){
            zRotation = 0.5;
        }
        if(zRotation<-0.5){
            zRotation = -0.5;
        }*/
        
        //Drift offset code
        // if(xSpeed>0){
        //     zRotation += SmartDashboard.getNumber("driftOffset", 0);
        // }
        // else if (xSpeed<0){
        //     zRotation -= SmartDashboard.getNumber("driftOffset", 0);
        // }


        m_drive.arcadeDrive(xSpeed*.85, -zRotation * .85, true);
    }

    //Returns the average encoder rotation of the 3 encoders converted to inches
    public double getAverageEncoderDistanceInches(){
        return ChassisConstants.kEncoderConversionFactor * (m_leftRearEncoder.getPosition() + m_rightFrontEncoder.getPosition() + m_rightRearEncoder.getPosition())/3;
    }

    //Returns the average encoder rotation of the 3 encoders
    public double getAverageEncoderPosition(){
        return (m_leftRearEncoder.getPosition() + m_rightFrontEncoder.getPosition() + m_rightRearEncoder.getPosition()) / 3;
    }

    //Returns the average rotation of the robot by subtracting the average of the right side from the average of the left side
    public double getAverageEncoderRotation(){
        return ((m_leftRearEncoder.getPosition()) - ((m_rightFrontEncoder.getPosition() + m_rightRearEncoder.getPosition())/2));
    }

    //Might cause DifferentialDrive errors if called
    public void resetEncoders(){
        m_leftFrontEncoder.setPosition(0.0);
        m_rightFrontEncoder.setPosition(0.0);
        m_rightRearEncoder.setPosition(0.0);
        m_leftRearEncoder.setPosition(0.0);
    }

    //This is called every 20ms
    @Override
    public void periodic(){
        SmartDashboard.putNumber("LF_Enc",m_leftFrontEncoder.getPosition());
        SmartDashboard.putNumber("RF_Enc",m_rightFrontEncoder.getPosition());
        SmartDashboard.putNumber("RR_Enc",m_rightRearEncoder.getPosition());
        SmartDashboard.putNumber("LR_Enc",m_leftRearEncoder.getPosition());

        SmartDashboard.putNumber("LF_Speed",m_leftFront.get());
        SmartDashboard.putNumber("RF_Speed",m_rightFront.get());
        SmartDashboard.putNumber("RR_Speed",m_rightRear.get());
        SmartDashboard.putNumber("LR_Speed",m_leftRear.get());

        SmartDashboard.putNumber("AveragePosition",this.getAverageEncoderPosition());
        SmartDashboard.putNumber("AverageRotation",this.getAverageEncoderRotation());
        SmartDashboard.putNumber("AveragePosition(inch)",this.getAverageEncoderDistanceInches());
    }
}
