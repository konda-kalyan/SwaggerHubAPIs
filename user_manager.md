## Functionality at High Level
* Responsible for Users onboarding/offboarding the end Users.
* Also manages Users Credentials. Request Issuers to issue the Credentials.
* Retrieves Credentials from Users Wallet Store.

## Prerequisites Softwares

### AWS VM
* Clone a VM with decent configuration (at least t2.medium size)
* Make sure that follow ports are opened in security group: SSH (22), MongoDB (27027), PostgresDB (5432), Tail Server (6543), Agent groups (5000, 10000, 10001)

### Install Docker
```
sudo apt-get update; sudo apt install docker.io -y;
sudo usermod -aG docker $USER; docker --version;
sudo reboot;
Re-login
groups;
docker ps;
```
### Install Java 17
```
sudo apt update; sudo apt install -y openjdk-17-jdk openjdk-17-jre;
sudo vim /etc/environment
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
JRE_HOME=/usr/lib/jvm/java-17-openjdk-amd64/jre

java -version;

sudo apt install -y maven; mvn -version;

In case, you want to switch between the java versions:
sudo apt update; sudo apt install -y openjdk-8-jdk openjdk-8-jre;
sudo update-alternatives --config java;
```

## Dependent Components Setup

### OffchainDB (MongoDB)
```
docker stop mongo_offchaindb; docker rm mongo_offchaindb;
docker run -d --name mongo_offchaindb -e MONGO_INITDB_ROOT_USERNAME=mongodbuser -e MONGO_INITDB_ROOT_PASSWORD=mongodbpassword -p 27017:27017 mongo:latest;
docker ps;
```
### PostgresDB (Aries Agent Wallet)
```
docker stop verifier_wallet_db; docker rm verifier_wallet_db;
docker run --name verifier_wallet_db -e POSTGRES_USER=myverifier -e POSTGRES_PASSWORD=Myverifier567 -e POSTGRES_DB=postgres -p 5432:5432 -d postgres:latest;
```
### Tailserver
```
docker stop tail_server; docker rm tail_server;
docker run -d --name tail_server -p 6543:6543 150100058/docker_tails-server:latest tails-server "$@" --host 0.0.0.0 --port 6543 --storage-path /tmp/tails-files --log-level INFO;
```
### Holder Aries Agent
```
docker stop holder_aries_agent; docker rm holder_aries_agent;
docker run -d --name holder_aries_agent -p 5001:5001 -p 10001:10001 kondakalyan/aca-py-connectionless-issuance start -it http 0.0.0.0 10001 -ot http --admin 0.0.0.0 5001 -e "<VM's URL/Public-IP>:10001" --multitenant --multitenant-admin --jwt-secret multitenancysecret12 --admin-api-key agentsecretapikey --wallet-type askar --seed 0000000baajidaasprojecttrueid111 --genesis-url "http://test.bcovrin.vonx.io/genesis" --label 'Holder Agent' --auto-provision --log-level info --auto-respond-messages --auto-accept-invites --auto-accept-requests --auto-respond-credential-offer --auto-respond-credential-request --auto-verify-presentation --auto-respond-presentation-request --auto-store-credential --preserve-exchange-records --wallet-name IDaaSHolderWallet-Askar --wallet-key dicept_key --tails-server-base-url "<VM's URL/Public-IP>:6543" --webhook-url "<VM's URL/Public-IP>:8081/webhooks" --wallet-storage-type postgres_storage --wallet-storage-config '{"url":"v:5432","wallet_scheme":"DatabasePerWallet"}' --wallet-storage-creds '{"account":"myverifier","password":"Myverifier567","admin_account":"myverifier","admin_password":"Myverifier567"}';
docker logs -f holder_aries_agent;
```
### Blockchain Network
In our echo system, we are using open source BCovrin Test network.

## Technical Flows/APIs

### Onboard/Signup User
