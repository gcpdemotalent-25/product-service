### GIT
echo "# product-service" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/gcpdemotalent-25/product-service.git
git push -u origin main

export PROJECT_ID=$(gcloud config get-value project)
echo $PROJECT_ID

### 1. Configuration initiale du projet GCP

# Définir votre ID de projet (remplacez PROJECT_ID)
gcloud config set project PROJECT_ID

# Définir une région par défaut
gcloud config set compute/region europe-west1

# Activer les APIs nécessaires
gcloud services enable run.googleapis.com \
sqladmin.googleapis.com \
artifactregistry.googleapis.com \
cloudbuild.googleapis.com

### 2.Créer l'instance Cloud SQL (MySQL)
# Créer une instance MySQL (cela peut prendre quelques minutes)
gcloud sql instances create microservices-db --database-version=MYSQL_8_0 --region=europe-west1 --root-password="mi@nDR1s04"

# Créer les bases de données pour chaque service
gcloud sql databases create produit_db --instance=microservices-db
gcloud sql databases create commande_db --instance=microservices-db

# Récupérer le nom de connexion de l'instance (gardez-le précieusement)
gcloud sql instances describe microservices-db --format='value(connectionName)'
# Résultat attendu : VOTRE_PROJECT_ID:europe-west1:microservices-db (gcp-project-20250702:europe-west1:microservices-db)

### 4.Construire et pousser les images Docker vers Artifact Registry
# Créer un dépôt Docker dans Artifact Registry
gcloud artifacts repositories create microservices-repo --repository-format=docker --location=europe-west1

# Uploader votre code source dans Cloud Shell (via l'icône "Upload File")
# Ou clonez-le depuis un dépôt Git.

# Construire et pousser chaque service (répétez pour les 3)
# Remplacer 'produit-service' par le nom du dossier
cd chemin/vers/produit-service
gcloud builds submit --tag europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/product-service:v1

cd ../commande-service
gcloud builds submit --tag europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/commande-service:v1

cd ../notification-service
gcloud builds submit --tag europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/notification-service:v1

### 5.Déployer les microservices sur Cloud Run
# 1. Déployer produit-service
gcloud run deploy produit-service \
--image=europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/produit-service:v1 \
--platform=managed \
--region=europe-west1 \
--allow-unauthenticated \
--add-cloudsql-instances=gcp-project-20250702:europe-west1:microservices-db

=> url : https://produit-service-71332585849.europe-west1.run.app/

# 2. Déployer notification-service (pas de connexion SQL)
gcloud run deploy notification-service \
--image=europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/notification-service:v1 \
--platform=managed \
--region=europe-west1 \
--allow-unauthenticated

# Récupérer les URLs des services déployés
PRODUIT_URL=$(gcloud run services describe produit-service --platform=managed --region=europe-west1 --format='value(status.url)')
NOTIFICATION_URL=$(gcloud run services describe notification-service --platform=managed --region=europe-west1 --format='value(status.url)')

# 3. Déployer commande-service en injectant les URLs des autres services
gcloud run deploy commande-service \
--image=europe-west1-docker.pkg.dev/$PROJECT_ID/microservices-repo/commande-service:v1 \
--platform=managed \
--region=europe-west1 \
--allow-unauthenticated \
--add-cloudsql-instances=gcp-project-20250702:europe-west1:microservices-db \
--set-env-vars="service.produit.url=$PRODUIT_URL,service.notification.url=$NOTIFICATION_URL"