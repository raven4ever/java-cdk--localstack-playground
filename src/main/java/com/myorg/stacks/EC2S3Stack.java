package com.myorg.stacks;

import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import com.myorg.utils.Utils;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.AmazonLinux2ImageSsmParameterProps;
import software.amazon.awscdk.services.ec2.AmazonLinuxEdition;
import software.amazon.awscdk.services.ec2.AmazonLinuxStorage;
import software.amazon.awscdk.services.ec2.AmazonLinuxVirt;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.InstanceProfile;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;

public class EC2S3Stack extends Stack {
        public EC2S3Stack(final Construct scope, final String id, final StackProps props,
                        final Map<String, String> values) {
                super(scope, id, props);

                String appName = values.get("appName");
                String bucketName = String.format("%s-bucket", appName).toLowerCase();
                String iamRoleName = String.format("%s-ec2-role", appName).toLowerCase();
                String iamProfileName = String.format("%s-ec2-profile", appName).toLowerCase();
                String ec2SGName = String.format("%s-sg", appName).toLowerCase();
                String ec2Name = String.format("%s-ec2", appName).toLowerCase();

                Tags.of(this).add("department", values.get("department"));
                Tags.of(this).add("application", values.get("appName"));

                // create common Bucket
                Bucket myS3Bucket = Bucket.Builder
                                .create(this, bucketName)
                                .bucketName(bucketName)
                                .versioned(true)
                                .blockPublicAccess(BlockPublicAccess.Builder
                                                .create()
                                                .blockPublicAcls(true)
                                                .build())
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .build();

                // create VPC
                IVpc myVPC = Vpc.fromLookup(this, values.get("vpcID"),
                                VpcLookupOptions.builder()
                                                .vpcId(values.get("vpcID"))
                                                .build());

                // create EC2 IAM Role
                Role ec2IAMRole = Role.Builder
                                .create(this, iamRoleName)
                                .roleName(iamRoleName)
                                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                                .build();

                // add SSM Policy
                ec2IAMRole.addManagedPolicy(ManagedPolicy
                                .fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"));

                // add S3 read access
                ec2IAMRole.addToPolicy(PolicyStatement.Builder
                                .create()
                                .effect(Effect.ALLOW)
                                .actions(List.of("s3:*"))
                                .resources(List.of(myS3Bucket.getBucketArn()))
                                .build());

                // EC2 Instance Profile
                InstanceProfile ec2InstanceProfile = InstanceProfile.Builder
                                .create(this, iamProfileName)
                                .instanceProfileName(iamProfileName)
                                .role(ec2IAMRole)
                                .build();

                // create SG
                SecurityGroup ec2SG = SecurityGroup.Builder
                                .create(this, ec2SGName)
                                .securityGroupName(ec2SGName)
                                .vpc(myVPC)
                                .allowAllOutbound(false)
                                .build();

                ec2SG.addEgressRule(Peer.anyIpv4(), Port.tcp(443));

                // create the EC2 instance
                Instance myEC2Instance = Instance.Builder
                                .create(this, ec2Name)
                                .instanceName(ec2Name)
                                .instanceType(new InstanceType("t2.micro"))
                                .machineImage(MachineImage
                                                .latestAmazonLinux2(AmazonLinux2ImageSsmParameterProps.builder()
                                                                .edition(AmazonLinuxEdition.STANDARD)
                                                                .virtualization(AmazonLinuxVirt.HVM)
                                                                .storage(AmazonLinuxStorage.EBS)
                                                                .build()))
                                .role(ec2IAMRole)
                                .vpc(myVPC)
                                .securityGroup(ec2SG)
                                .build();

                // read user_data.sh file
                String userDataString = Utils.getFileContentAsString("user_data.sh");

                // add the user_data to the instance
                myEC2Instance.addUserData(userDataString);
        }
}
