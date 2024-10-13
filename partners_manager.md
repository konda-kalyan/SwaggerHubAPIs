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
* Make sure that follow ports are opened in security group: SSH (22), MongoDB (27027), PostgresDB (5432), Tail Server (6543), Isser and Verifier Agent ports (5002, 10002, 5000, 10000)

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
docker run -d --name verifier_aries_agent -p 5000:5000 -p 10000:10000 kondakalyan/aca-py-connectionless-issuance start -it http 0.0.0.0 10000 -ot http --admin 0.0.0.0 5000 -e "<VM's URL/Public-IP>:10000" --multitenant --multitenant-admin --jwt-secret multitenancysecret12 --admin-api-key agentsecretapikey --wallet-type askar --seed 0000000baajidaasprojecttrueid111 --genesis-url "http://test.bcovrin.vonx.io/genesis" --label 'Verifier Agent' --auto-provision --log-level info --auto-respond-messages --auto-accept-invites --auto-accept-requests --auto-respond-credential-offer --auto-respond-credential-request --auto-verify-presentation --auto-respond-presentation-request --auto-store-credential --preserve-exchange-records --wallet-name IDaaSVerifierWallet-Askar --wallet-key dicept_key --tails-server-base-url "<VM's URL/Public-IP>:6543" --webhook-url "<VM's URL/Public-IP>:7071/webhooks" --wallet-storage-type postgres_storage --wallet-storage-config '{"url":"13.127.169.103:5432","wallet_scheme":"DatabasePerWallet"}' --wallet-storage-creds '{"account":"myverifier","password":"Myverifier567","admin_account":"myverifier","admin_password":"Myverifier567"}';
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
docker run -d --name didvc-issuer-controller -p 9091:9091 -v /home/ubuntu/logs/DIDVCIssuerControllerLogs:/logs -e server.port=9091 -e aries.agent.api.endpoint.base.url="<VM's URL/Public-IP>:5002" -e spring.data.mongodb.host="13.127.169.103" -e user.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:8081" -e tail.server.url="<VM's URL/Public-IP>:6543" didvc-issuer-controller;
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
docker run -d --name didvc-verifier-controller -p 7071:7071 -v /home/ubuntu/logs/DIDVCVerifierControllerLogs:/logs -e server.port=7071 -e aries.agent.api.endpoint.base.url="<VM's URL/Public-IP>:5000" -e spring.data.mongodb.host="13.127.169.103" -e user.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:8081" -e tail.server.url="<VM's URL/Public-IP>:6543" didvc-verifier-controller;
docker logs -f didvc-verifier-controller;
```

## Bringup User Manager Application
Build the application
```
cd UserManager; 
mvn clean install;
docker build -t user-manager .;
```

Run/Bringup the application
```
docker stop user-manager; docker rm user-manager;
docker run -d --name user-manager -p 8080:8080 -v /home/ubuntu/logs/UserManagerLogs:/logs -e server.port=8080 -e spring.data.mongodb.host="<VM's URL/Public-IP>" -e user.did-vc.controller.api.endpoint.base.url="<VM's URL/Public-IP>:8081" user-manager;
docker logs -f user-manager;
```

## Technical Flows/APIs

### Onboard/Signup User
#### Flow
* Creates User account onto Baaj’s system. It expects User’s basic details such as Name, Email, Phone etc.
* Internally it creates DID and Wallet on Agent layer User.
* Stores User details on Users Wallet Store.
#### Endpoint
```
POST <IP/base_url>:8080/user/onboard
```
#### Request
```
{
    "name": "<user_name>",
    "email": "<user_email>",
    "phone_number": "<user_phone>",
    "age": 40
}
```
#### Response
```
{
    "id": "SBP7wtxXGgYcxd9ebM4AJy",
    "name": "BaaJ-User",
    "email": "konda.kalyan@gmail.com",
    "phone_number": "+917675025060",
    "age": 40
}
```

### Request Credential
#### Flow
* This is asynchronous operation.
* User requests particular Issuer to issue Credentials based on claims provided.
* Issuer verifies User’s claims and issues VC. Issue can take their own time from minutes to days/weeks. In our PoC, it is assumed that Issuer is always agreed to issue Credential.
* Credential details get stored on both User’s and Issuer's stores.

#### Endpoint
```
POST <IP/base_url>:8080/credential/request-credential
```
#### Request
```
{
	  "user_id": "<user_id>",
	  "name": "<user_name>",
	  "email": "<user_email>",
	  "issuer_name": "<issuer_name>",
	  "issuer_id": "<issuer_id>",
	  "schema_name": "<schema_name>",
	  "schema_version": "<schema_version>",
	  "schema_id": "<schema_id>",
	  "credential_data": [
		{
		  "name": "Name",
		  "value": "BaaJ-User"
		},
		{
		  "name": "Email",
		  "value": "konda.kalyan@gmail.com"
		},
		{
		  "name": "Phone Number",
		  "value": "+911234567890"
		},
		{
		  "name": "Date Of Birth",
		  "value": "16-Jul-1981"
		}
	  ]
	}
```
#### Response
```
{
    "id": "SBP7wtxXGgYcxd9ebM4AJy",
    "name": "BaaJ-User",
    "email": "konda.kalyan@gmail.com",
    "phone_number": "+917675025060",
    "age": 40
}
```

### Retrives User's Crednetials
#### Flow
* Couple of read/get operations to retrieve User’s Credentials for given name or email.
#### Endpoint
```
GET <IP/base_url>:8080/credential/retrieve-credentials/name/{user_name}
GET <IP/base_url>:8080/credential/retrieve-credentials/email/{user_email}
```
#### Request
```
<Nonthing>
```
#### Response
```

```
