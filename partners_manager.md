## Functionality at High Level
* Responsible for onboarding/offboarding the Baaj Partners (Issuers and Verifiers / Relying Party).
* Also manages Issuing & Verifying Credentials.
* Retrieves Credentials from Users Wallet Store.
* Creates Credential Schemas (which are used to issue VCs). Schema is the nothing but the meta data (attributes) of Credential to be issued. Example: Degree Certificate is Schema and it attributes are Student Name, College Name, University Name, Specialization, Result (pass/fail), Percentage/Score, Year of Completion.
* Retries Schemas from Issuer wallet.
* Verifies Credentials.

## Prerequisite Softwares

### AWS VM
* Clone a VM with decent configuration (at least t2.medium size)
* Make sure that follow ports are opened in security group: SSH (22), MongoDB (27027), PostgresDB (5432), Tail Server (6543), Isser and Verifier Agent ports (5002, 10002, 5000, 10000), Issuer Controller (9091), Verifier Controller (7071), Partners Manager (9090)

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
### Issuer Aries Agent
```
docker stop issuer_aries_agent; docker rm issuer_aries_agent;
docker run -d --name issuer_aries_agent -p 5002:5002 -p 10002:10002 kondakalyan/aca-py-connectionless-issuance start -it http 0.0.0.0 10002 -ot http --admin 0.0.0.0 5002 -e "<VM's URL/Public-IP>:10002" --multitenant --multitenant-admin --jwt-secret multitenancysecret12 --admin-api-key agentsecretapikey --wallet-type askar --seed 0000000baajidaasprojecttrueid111 --genesis-url "http://test.bcovrin.vonx.io/genesis" --label 'Issuer Agent' --auto-provision --log-level info --auto-respond-messages --auto-accept-invites --auto-accept-requests --auto-respond-credential-offer --auto-respond-credential-request --auto-verify-presentation --auto-respond-presentation-request --auto-store-credential --preserve-exchange-records --wallet-name IDaaSIssuerWallet-Askar --wallet-key dicept_key --tails-server-base-url "<VM's URL/Public-IP>:6543" --webhook-url "<VM's URL/Public-IP>:9091/webhooks" --wallet-storage-type postgres_storage --wallet-storage-config '{"url":"<VM's URL/Public-IP>:5432","wallet_scheme":"DatabasePerWallet"}' --wallet-storage-creds '{"account":"myverifier","password":"Myverifier567","admin_account":"myverifier","admin_password":"Myverifier567"}';
docker logs -f issuer_aries_agent;
```
### Verifier Aries Agent
```
docker stop verifier_aries_agent; docker rm verifier_aries_agent;
docker run -d --name verifier_aries_agent -p 5000:5000 -p 10000:10000 kondakalyan/aca-py-connectionless-issuance start -it http 0.0.0.0 10000 -ot http --admin 0.0.0.0 5000 -e "<VM's URL/Public-IP>:10000" --multitenant --multitenant-admin --jwt-secret multitenancysecret12 --admin-api-key agentsecretapikey --wallet-type askar --seed 0000000baajidaasprojecttrueid111 --genesis-url "http://test.bcovrin.vonx.io/genesis" --label 'Verifier Agent' --auto-provision --log-level info --auto-respond-messages --auto-accept-invites --auto-accept-requests --auto-respond-credential-offer --auto-respond-credential-request --auto-verify-presentation --auto-respond-presentation-request --auto-store-credential --preserve-exchange-records --wallet-name IDaaSVerifierWallet-Askar --wallet-key dicept_key --tails-server-base-url "<VM's URL/Public-IP>:6543" --webhook-url "<VM's URL/Public-IP>:7071/webhooks" --wallet-storage-type postgres_storage --wallet-storage-config '{"url":"<VM's URL/Public-IP>:5432","wallet_scheme":"DatabasePerWallet"}' --wallet-storage-creds '{"account":"myverifier","password":"Myverifier567","admin_account":"myverifier","admin_password":"Myverifier567"}';
docker logs -f verifier_aries_agent;
```
### Blockchain Network
In our echo system, we are using open source BCovrin Test network.

### Issuer Controller Application
Build the application
```
mvn clean install;
docker build -t didvc-issuer-controller .;
```
Run/Bringup the application
```
docker stop didvc-issuer-controller; docker rm didvc-issuer-controller;
docker run -d --name didvc-issuer-controller -p 9091:9091 -v /home/ubuntu/logs/DIDVCIssuerControllerLogs:/logs -e server.port=9091 -e aries.agent.api.endpoint.base.url="<VM's URL/Public-IP>:5002" -e spring.data.mongodb.host="<VM's URL/Public-IP>" -e user.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:8081" -e tail.server.url="<VM's URL/Public-IP>:6543" didvc-issuer-controller;
docker logs -f didvc-issuer-controller;
```
### Verifier Controller Application
Build the application
```
mvn clean install;
docker build -t didvc-verifier-controller .;
```
Run/Bringup the application
```
docker stop didvc-verifier-controller; docker rm didvc-verifier-controller;
docker run -d --name didvc-verifier-controller -p 7071:7071 -v /home/ubuntu/logs/DIDVCVerifierControllerLogs:/logs -e server.port=7071 -e aries.agent.api.endpoint.base.url="<VM's URL/Public-IP>:5000" -e spring.data.mongodb.host="<VM's URL/Public-IP>" -e user.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:8081" -e tail.server.url="<VM's URL/Public-IP>:6543" didvc-verifier-controller;
docker logs -f didvc-verifier-controller;
```

## Bringup Partners Manager Application
Build the application
```
mvn clean install;
docker build -t partners-manager .;
```
Run/Bringup the application
```
docker stop partners-manager; docker rm partners-manager;
docker run -d --name partners-manager -p 9090:9090 -v /home/ubuntu/logs/PartnersManagerLogs:/logs -e server.port=9090 -e spring.data.mongodb.host="<VM's URL/Public-IP>" -e issuer.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:9091" -e verifier.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:7071" partners-manager;
docker logs -f partners-manager;
```

## Technical Flows/APIs

### Onboard Issuer
#### Flow
* Onboard Issuer onto Baaj’s system. It expects Issuer’s basic details such as Name, Email, Phone etc.
* Internally it creates DID and Wallet on Agent layer User.
* Register Issuer on Blockchain network.
* Stores Issuer details on Partners Wallet Store.
#### Endpoint
```
Partners Manager – POST <IP/base_url>:9090/issuer/onboard
```
#### Request
```
{
  "name": "string",
  "email": "string",
  "phone_number": "string"
}
```
#### Response
```
{
  "id": "string",
  "name": "string",
  "email": "string",
  "phone_number": "string"
}
```

### Onboard Verifier
#### Flow
* This is asynchronous operation.
* User requests particular Issuer to issue Credentials based on claims provided.
* Issuer verifies User’s claims and issues VC. Issue can take their own time from minutes to days/weeks. In our PoC, it is assumed that Issuer is always agreed to issue Credential.
* Credential details get stored on both User’s and Issuer's stores.

#### Endpoint
```
POST <IP/base_url>:9090/verifier/onboard
```
#### Request
```
{
  "name": "string",
  "email": "string",
  "phone_number": "string"
}
```
#### Response
```
{
  "id": "string",
  "name": "string",
  "email": "string",
  "phone_number": "string"
}
```

### Create Credential Schema
#### Flow
* Isser creates Credential Schema on Agent wallet.
* Register Schema info on Ledger.
* Stores Schema details on Partners Issuers store.
  
#### Endpoint
```
POST <IP/base_url>:9090/schema
```
#### Request
```
{
  "issuer_id": "string",
  "name": "string",
  "version": "string",
  "attributes": [
    "string"
  ]
}
```
#### Response
```
{
  "id": "string",
  "issuer_id": "string",
  "name": "string",
  "version": "string",
  "seqNo": "string",
  "attributes": [
    "string"
  ],
  "created_at": "2024-10-13T08:10:50.729Z",
  "cred_def_id": "string",
  "cred_def_tag": "string",
  "rev_reg_id": "string"
}
```
### Verify Credential
#### Flow
* This is asynchronous operation.
* Verifies Credential from User.
* Verifier can mention what kind of Credential (Example: I want to verify Name and Address from Driver’s License which was issued by State Government) that it wants to verify.
  
#### Endpoint
```
POST <IP/base_url>:9090/verification/verify-credential
```
#### Request
```
{
  "user_email": "string",
  "proof_request": {
    "requested_attributes": {
      "additionalProp1": {
        "name": "string",
        "restrictions": [
          {
            "additionalProp1": "string",
            "additionalProp2": "string",
            "additionalProp3": "string"
          }
        ]
      }
    },
    "requested_predicates": {
      "additionalProp1": {
        "name": "string",
        "p_type": "string",
        "p_value": 0,
        "restrictions": [
          {
            "additionalProp1": "string",
            "additionalProp2": "string",
            "additionalProp3": "string"
          }
        ]
      } 
  },
  "special_verification": true,
  "special_attribute": "string",
  "special_condition": "string",
  "special_comparision_value": "string",
  "request_id": "string"
}
```
#### Response
```
UUID
```
