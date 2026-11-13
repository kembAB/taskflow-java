# Deploying TaskFlow to Azure - ACR and AKS

TaskFlow ships as one Docker image. On Azure the image is stored in
Azure Container Registry and run on Azure Kubernetes Service, using the
same manifests that live in the `k8s` folder.

## 1. Create the resource group and the registry

```bash
az group create --name taskflow-rg --location eastus

az acr create \
  --resource-group taskflow-rg \
  --name taskflowabel \
  --sku Basic

az acr login --name taskflowabel
```

## 2. Build the image in ACR and push it

Option A - let ACR build the image from the Dockerfile:

```bash
az acr build \
  --registry taskflowabel \
  --image taskflow:1.0.0 \
  --file Dockerfile .
```

Option B - build locally and push:

```bash
docker build -t taskflowabel.azurecr.io/taskflow:1.0.0 .
docker push taskflowabel.azurecr.io/taskflow:1.0.0
```

## 3. Create the AKS cluster and attach the registry

```bash
az aks create \
  --resource-group taskflow-rg \
  --name taskflow-aks \
  --node-count 2 \
  --generate-ssh-keys \
  --attach-acr taskflowabel

az aks get-credentials \
  --resource-group taskflow-rg \
  --name taskflow-aks
```

## 4. Deploy with the Kubernetes manifests

Edit the image name in `k8s/deployment.yaml` so it points at the ACR
image, then apply everything:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

kubectl rollout status deployment/taskflow
kubectl get pods,svc,ingress
```

The app reads its PostgreSQL settings from the ConfigMap and the Secret.
The same manifests also run the PostgreSQL database inside the cluster
for demos. For production, replace the in-cluster database with Azure
Database for PostgreSQL and update the ConfigMap URL.

## 5. GitHub Actions

The workflow in `.github/workflows/ci-cd.yml` repeats these steps on
every push to `main`. It needs these repository secrets:

- `AZURE_CREDENTIALS` - service principal JSON from `az ad sp create-for-rbac`
- `ACR_NAME` and `ACR_LOGIN_SERVER`
- `AKS_RESOURCE_GROUP` and `AKS_CLUSTER_NAME`
