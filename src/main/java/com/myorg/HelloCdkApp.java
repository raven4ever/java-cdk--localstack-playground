package com.myorg;

import java.util.Map;
import java.util.Optional;

import com.myorg.stacks.EC2S3Stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;

public class HelloCdkApp {
        public static void main(final String[] args) {
                App app = new App();

                Map<String, String> values = Map.ofEntries(
                                Map.entry("department", "French JSON"),
                                Map.entry("vpcID", "vpc-721bc9e4"),
                                Map.entry("appName", "demoApp"));

                String awsRegion = Optional
                                .ofNullable(System.getenv("AWS_REGION"))
                                .orElse("eu-west-1");
                String accountNumber = Optional
                                .ofNullable(System.getenv("AWS_ACCOUNT"))
                                .orElse("000000000000");

                EC2S3Stack myStack = new EC2S3Stack(app, "EC2S3Stack", StackProps
                                .builder()
                                .env(Environment.builder()
                                                .region(awsRegion)
                                                .account(accountNumber)
                                                .build())
                                .build(),
                                values);

                Tags.of(myStack).add("createdBy", "Adrian");
                Tags.of(myStack).add("managedBy", "AWS CDK - Java");

                app.synth();
        }
}
