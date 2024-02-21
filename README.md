# AWS CDK with Java :heart_eyes:

## Requirements

- `JDK`
- `Python 3`
- `NPM`
- `AWS CDK`
- `Apache Maven`
- `Docker`
- `Docker Compose`
- `awslocal`
- `cdklocal`

```shell
python3 -m pip install awscli-local
npm install -g aws-cdk-local aws-cdk
```

## Setup

The stack assumes the VPC and the subnets are already created.

While using `Localstack`, you can run the following to create an initial setup.

```shell
MY_AWS_REGION="eu-west-1"
VPC_ID=$(awslocal ec2 create-vpc --cidr-block 192.168.13.0/24 --region $MY_AWS_REGION --query Vpc.VpcId --output text)
PUB_SUBNET_ID=$(awslocal ec2 create-subnet --vpc-id $VPC_ID --cidr-block 192.168.13.0/26 --availability-zone eu-west-1a --region $MY_AWS_REGION --query Subnet.SubnetId --output text)
awslocal ec2 create-subnet --vpc-id $VPC_ID --cidr-block 192.168.13.64/26 --availability-zone eu-west-1b --region $MY_AWS_REGION --query Subnet.SubnetId --output text
awslocal ec2 create-subnet --vpc-id $VPC_ID --cidr-block 192.168.13.128/26 --availability-zone eu-west-1c --region $MY_AWS_REGION --query Subnet.SubnetId --output text
IGW_ID=$(awslocal ec2 create-internet-gateway --region $MY_AWS_REGION --query InternetGateway.InternetGatewayId --output text)
awslocal ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID --region $MY_AWS_REGION
RTABLE_ID=$(awslocal ec2 create-route-table --vpc-id $VPC_ID --region $MY_AWS_REGION --query RouteTable.RouteTableId --output text)
awslocal ec2 create-route --route-table-id $RTABLE_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID --region $MY_AWS_REGION
awslocal ec2 associate-route-table --route-table-id $RTABLE_ID --subnet-id $PUB_SUBNET_ID --region $MY_AWS_REGION
```

## CDK

### Local with Localstack

```shell
docker compose up -d
export AWS_REGION="eu-west-2"
export AWS_ACCOUNT="000000000000"
cdklocal synth
cdklocal bootstrap
cdklocal deploy
```

Make sure you use the same region as for the VPC creation!

### AWS

```shell
cdk synth
cdk bootstrap
cdk deploy
```

## Cleanup

```shell
mvn clean
cdklocal destroy
cdk destroy
```
