package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaCurrentGame;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.TfodCurrentGame;

@Autonomous(name = "ActualMainAuto")
public class ActualMainAuto extends LinearOpMode {

    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";
    private static final String VUFORIA_KEY = "ASDZYK3/////AAABmcPs4lemSEC6kHBUD5lpAQRxx5U+vJZBQeR+erp7dMnyf5eZnu5FO9XXrTfzX2BSltfkw3Rn2lpjoxhIk12n7fFiRQoi0CLQFbulEDE4FXJxJZWD5jHsDsn3J/Dp4flFNSmUiGDe88clC0BEplIXOEdqPqr1JV4WN2fR/9jVDFt2BUW/l+hI42++7hmaEgmPb69uIvOVpIVLvTqUJ4i78PxlGHW+uYj6tZjEYJbJBQrJC/YBJ2K2MP+iA+UVZhfirrDXct87srSKAim4p0EHNMvdUczqh29L/+5KZUHtGGYuNvZAvp/rbceDlgz8EIoxHgxTMWuAfGJeD0lfv+nJ5zooRNqLYeEO6SnIr6oAvsHV";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    private DcMotor backRight;
    private DcMotor frontRight;
    private DcMotor frontLeft;
    private DcMotor backLeft;
    private DcMotor firstLauncher;
    private DcMotor secondLauncher;
    private Servo lifter;
    private Servo shifter;

    //Inits Vuforia
    
    private void initVuforia() {

        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;                               //Inits Vuforia
        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }  
    
    //Inits Tfod
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);   
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
        tfod.activate();
    } 
    
    //Sets both launcher motors to specified power
    private void setShooterPower(double power) {
        firstLauncher.setPower(power);
        secondLauncher.setPower(power);                                     
        sleep(1000);
    }

    //Sets motor encoders to STOP_AND_RESET
    private void stopAndReset() {
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);         
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);  
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    } 

    //Sets motor encoders to RUN_TO_POSITION
    private void runToPosition() {
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);                 
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    } 

    //Sets target motor position. To be used between stopAndReset() and runToPosition()
    private void setMotorPosition(int position) {
        backRight.setTargetPosition(position);
        frontRight.setTargetPosition(position);                              
        backLeft.setTargetPosition(position);            
        frontLeft.setTargetPosition(position);
    }

    //Inits actuators
    private void initMotorsServos() {

        backRight = hardwareMap.get(DcMotor.class, "backRight");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");

        firstLauncher = hardwareMap.get(DcMotor.class, "firstLauncher");
        secondLauncher = hardwareMap.get(DcMotor.class, "secondLauncher");   

        lifter = hardwareMap.get(Servo.class, "lifter");
        shifter = hardwareMap.get(Servo.class, "shifter");                                                   

        lifter.setPosition(0.87);
        shifter.setPosition(1);

        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        firstLauncher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        secondLauncher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        stopAndReset();

    }          

    //Returns A, B, or C depending on amount of rings 
    private String gettargetZone() {

        String targetZone;

        // Get a list of recognitions from TFOD.
        List<Recognition> recognitions = tfod.getUpdatedRecognitions();     

        if (recognitions.size() == 0) {
            telemetry.addData("Target Zone:", "A");
            targetZone = "A";
        } else {
            if (recognitions.get(0).getLabel().equals("Quad")) {
                telemetry.addData("Target Zone:", "C");
                targetZone = "C";
            } else {
                telemetry.addData("Target Zone:", "B");
                targetZone = "B";
            }
        }
        telemetry.update();
        return targetZone;
    }     

    //Initial movement of robot in auto. Move forward to stack with specified position and power.
    private void initialMovement(double power, int distance) {

        setMotorPosition(distance);

        runToPosition();

        backRight.setPower(power);                                          
        frontRight.setPower(power);
        backLeft.setPower(power);
        frontLeft.setPower(power);

        while (backRight.isBusy() && backLeft.isBusy() && frontRight.isBusy() && frontLeft.isBusy()) {
            //Waits for the motors to finish moving & prints 2 CurrentPositions
            telemetry.addData("FrontLeft Position: ", frontLeft.getCurrentPosition() + "  busy=" + frontLeft.isBusy());
            telemetry.addData("FrontRight Position: ", frontRight.getCurrentPosition() + "  busy=" + frontRight.isBusy());
            telemetry.update();
        }

        backRight.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        frontLeft.setPower(0);

        stopAndReset();

    }

    //Empties 3 rings at specified power
    private void shoot(double power) {
        setShooterPower(power);
        telemetry.addData("power", firstLauncher.getPower());            
        telemetry.update();
        for (int i = 0; i < 3; i++) {
            shifter.setPosition(0.57);
            sleep(500);
            shifter.setPosition(1);
            sleep(500);
        }
        setShooterPower(0);
    }

    @Override
    public void runOpMode() {

        telemetry.addData("Ready:", "False");
        telemetry.update();
        initVuforia();
        initTfod();
        initMotorsServos();

        telemetry.addData("Ready:", "True");
        telemetry.update();
        waitForStart();
        if (opModeIsActive()) {

            shoot(0.8);

            initialMovement(0.8, 5000);

            if (tfod != null) {

                String targetZone = gettargetZone();
                sleep(5000);
                telemetry.addData("Target Zone:", targetZone);

                sleep(5000);


                if (targetZone.equals("A")) {
                    // Go to zone A
                } else if (targetZone.equals("B")) {
                    // Go to Zone B
                } else {
                    // Go to Zone C
                }
            } else {
                //Go to zone A
            }

        }
        // Deactivate TFOD.
        if (tfod != null) {
            tfod.deactivate();
        }
    }

}
