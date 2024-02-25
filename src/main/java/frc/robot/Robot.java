// CHRIS BENNETT
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot; //declares this to be an FRC robot, permitting use of FRC class

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kNothingAuto = "do nothing";
  private static final String kMoveOnly = "move only";
  private static final String kCone = "cone";
  private static final String kCube = "cube"; 
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  CANSparkMax driveLeftSpark1  = new CANSparkMax(1, MotorType.kBrushless);
  CANSparkMax driveLeftSpark3  = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkMax driveRightSpark2 = new CANSparkMax(2, MotorType.kBrushless);
  CANSparkMax driveRightSpark4 = new CANSparkMax(4, MotorType.kBrushless);

  CANSparkMax arm = new CANSparkMax(5, MotorType.kBrushless);
  CANSparkMax intake = new CANSparkMax(6, MotorType.kBrushless);
  
  Joystick jDrive = new Joystick(0);
  Joystick jArm = new Joystick(1);

  //arm power and current limit
  static final int ARM_CURRENT_LIMIT_A = 20;
  static final double ARM_OUTPUT_POWER = 0.4;

  //intake constants
  static final int INTAKE_CURRENT_LIMIT_A = 25;
  static final int INTAKE_HOLD_CURRENT_LIMIT_A = 5;
  static final double INTAKE_OUTPUT_POWER = 1.0;
  static final double INTAKE_HOLD_POWER = 0.07; 

  //autonomous mode constants
  static final double MOVE_ONLY_TIME = 2.5;
  static final double AUTO_DRIVE_SPEED = -0.25;
  static final double ARM_EXTEND_TIME = 1.1;
  static final double AUTO_THROW_TIME = .4;
  static final double AUTO_DRIVE_TIME = 3.85;

    
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Do Nothing", kNothingAuto);
    m_chooser.addOption("Move Only", kMoveOnly);
    m_chooser.addOption("Cube and Mobility", kCube);
    m_chooser.addOption("Cone and Mobility", kCone);
    SmartDashboard.putData("Auto choices", m_chooser);

    driveLeftSpark1.setInverted(true);
    driveLeftSpark3.setInverted(true);
    driveRightSpark2.setInverted(false);
    driveRightSpark4.setInverted(false);

    arm.setInverted(false);
    arm.setIdleMode(IdleMode.kBrake);
    arm.setSmartCurrentLimit(ARM_CURRENT_LIMIT_A);

    intake.setInverted(false);
    intake.setIdleMode(IdleMode.kBrake);
  }

  public void setDriveMotors(double forward, double turn){
    SmartDashboard.putNumber("drive forward power (%)", forward);
    SmartDashboard.putNumber("drive turn power (%)", turn);

    double left = forward + turn;
    double right = forward - turn;

    SmartDashboard.putNumber("drive left power (%)", left);
    SmartDashboard.putNumber("drive right power (%)", right);

    driveLeftSpark1.set(left);
    driveLeftSpark3.set(left);
    driveRightSpark2.set(right);
    driveRightSpark4.set(right); 
  }

  public void setArmMotor(double percent){
    arm.set(percent);
    SmartDashboard.putNumber("arm power (%)", percent);
    SmartDashboard.putNumber("arm motor current (amps)", arm.getOutputCurrent());
    SmartDashboard.putNumber("arm motor temperature (C)", arm.getMotorTemperature());
  }

    public void setIntakeMotor(double percent, int amps){
      intake.set(percent);
      intake.setSmartCurrentLimit(amps);

      SmartDashboard.putNumber("Intake Power (%)", percent);
      SmartDashboard.putNumber("Intake motor current (amps)", intake.getOutputCurrent());
      SmartDashboard.putNumber("Intake motor temperature (C)", intake.getMotorTemperature());
    }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}


  double autoStartTime;
  double autoIntakePower;
  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */

  /** autonomousInit() runs once before autonomous mode starts */
  @Override
  public void autonomousInit() { 
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    
    driveLeftSpark1.setIdleMode(IdleMode.kBrake);
    driveLeftSpark3.setIdleMode(IdleMode.kBrake);
    driveRightSpark2.setIdleMode(IdleMode.kBrake);
    driveRightSpark4.setIdleMode(IdleMode.kBrake);

    autoStartTime = Timer.getFPGATimestamp();

    if(m_autoSelected == kCone){
      autoIntakePower = INTAKE_OUTPUT_POWER;
    }
    else{
      autoIntakePower = -INTAKE_OUTPUT_POWER;
    } 
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    double elaspedTime = Timer.getFPGATimestamp() - autoStartTime;
    if (m_autoSelected == kNothingAuto){
      setDriveMotors(0.0, 0.0);
      setArmMotor(0.0);
      setIntakeMotor(0.0, ARM_CURRENT_LIMIT_A);
    }
    else if(m_autoSelected == kMoveOnly){
        if (elaspedTime < MOVE_ONLY_TIME){
          setDriveMotors(0.0, AUTO_DRIVE_TIME);
          setArmMotor(0.0);;
          setIntakeMotor(0.0, ARM_CURRENT_LIMIT_A);
        }
        else{
          setDriveMotors(0.0, 0.0);
          setArmMotor(0.0);
          setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
        }
    }
    else{
      //cone and cube / mobility autos both are the same because we set the direction of the intake in auto init.
      if (elaspedTime < ARM_EXTEND_TIME){ //extend the arm
        setDriveMotors(0.0, 0.0);
        setArmMotor(ARM_OUTPUT_POWER);
        setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
      }
      else if(elaspedTime < ARM_EXTEND_TIME + AUTO_THROW_TIME){ //throw game piece
        setDriveMotors(0.0, 0.0);
        setArmMotor(0);
        setIntakeMotor(autoIntakePower, ARM_CURRENT_LIMIT_A);
      }
      else if (elaspedTime < ARM_EXTEND_TIME + AUTO_THROW_TIME + ARM_EXTEND_TIME){ //retract the arm
        setDriveMotors(0.0, 0.0);
        setArmMotor(-ARM_OUTPUT_POWER);
        setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
      }
      else if (elaspedTime < ARM_EXTEND_TIME + AUTO_THROW_TIME + ARM_EXTEND_TIME + AUTO_DRIVE_TIME){
        setDriveMotors(AUTO_DRIVE_SPEED, 0);
        setArmMotor(0.0 );
        setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
      }
      else{
      setDriveMotors(0.0, 0.0);
      setArmMotor(0.0);
      setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
      }
    }
  }

  static final int CONE = 1;
  static final int CUBE = 2;
  static final int NOTHING = 3;
  int lastGamePiece;
  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    driveLeftSpark1.setIdleMode(IdleMode.kCoast);
    driveLeftSpark3.setIdleMode(IdleMode.kCoast);
    driveRightSpark2.setIdleMode(IdleMode.kCoast);
    driveRightSpark4.setIdleMode(IdleMode.kCoast);

    lastGamePiece = NOTHING;
  }

  /** This function is called periodically during operator control. CMB */
  @Override
  public void teleopPeriodic() {
    setDriveMotors(-jDrive.getRawAxis(1), jDrive.getRawAxis( 4));

    double armPower;
    if (jArm.getRawButton(4)){
      armPower = -ARM_OUTPUT_POWER;
    }
    else if (jArm.getRawButton(5)){
      armPower = ARM_OUTPUT_POWER;
    }
    else{
      armPower = 0.0;
    }
    setArmMotor(armPower);

    double intakePower;
    int intakeAmps;
    if(jArm.getRawButton(1)){
      //cube in and cone out
      intakePower = INTAKE_OUTPUT_POWER;
      intakeAmps = INTAKE_CURRENT_LIMIT_A;
      lastGamePiece = CUBE;
    }
    else if (jArm.getRawButton(4)){
      //cone in and cube out
      intakePower = -INTAKE_OUTPUT_POWER;
      intakeAmps = INTAKE_CURRENT_LIMIT_A;
      lastGamePiece = CONE;
    }
    else if (lastGamePiece == CUBE){
      intakePower = INTAKE_HOLD_POWER;
      intakeAmps = INTAKE_HOLD_CURRENT_LIMIT_A;
    }
    else if (lastGamePiece == CONE){
      intakePower = -INTAKE_HOLD_POWER;
      intakeAmps = INTAKE_HOLD_CURRENT_LIMIT_A;
    }
    else{
      intakePower = 0.0;
      intakeAmps = 0;
    }
    setIntakeMotor(intakePower, intakeAmps);
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
