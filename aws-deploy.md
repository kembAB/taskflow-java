# Deploying TaskFlow to AWS - ECR and ECS Fargate

TaskFlow ships as one Docker image. On AWS the image is stored in
Elastic Container Registry and run as a serverless container service on
ECS Fargate. The Kubernetes route on EKS works too, with the same
manifests from the `k8s` folder.

## 1. Create the ECR repository and push the image

```bash
aws ecr create-repository --repository-name taskflow --region us-east-1

aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    123456789012.dkr.ecr.us-east-1.amazonaws.com

docker build -t taskflow:1.0.0 .
docker tag taskflow:1.0.0 \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/taskflow:1.0.0
docker push \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/taskflow:1.0.0
```

Replace `123456789012` with your own AWS account ID.

## 2. Prepare the database

Create an Amazon RDS for PostgreSQL instance named `taskflow`, store its
password in AWS Secrets Manager as `taskflow/db-password`, and put the
RDS endpoint into `SPRING_DATASOURCE_URL` inside
`aws/ecs-task-definition.json`. Replace the placeholder image value
`REPLACE_WITH_ECR_IMAGE` with the full ECR image URI from step 1.

## 3. Register the task definition and create the Fargate service

```bash
aws ecs create-cluster --cluster-name taskflow-cluster

aws ecs register-task-definition \
  --cli-input-json file://aws/ecs-task-definition.json

aws ecs create-service \
  --cluster taskflow-cluster \
  --service-name taskflow-service \
  --task-definition taskflow \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration \
    "awsvpcConfiguration={subnets=[subnet-0123abcd],securityGroups=[sg-0123abcd],assignPublicIp=ENABLED}"
```

Put an Application Load Balancer in front of the service for a stable
public address, target port 8080.

## 4. The EKS option

The manifests in `k8s` run unchanged on Amazon EKS. Only the image name
in `k8s/deployment.yaml` changes, to the ECR image URI:

```bash
aws eks update-kubeconfig --name taskflow-eks --region us-east-1
kubectl apply -f k8s/
kubectl rollout status deployment/taskflow
```

## 5. GitHub Actions

The workflow in `.github/workflows/ci-cd.yml` automates the ECR push,
the EKS rollout and the ECS deployment on every push to `main`. It needs
these repository secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`,
`AWS_REGION`, `EKS_CLUSTER_NAME`, `ECS_CLUSTER` and `ECS_SERVICE`.
