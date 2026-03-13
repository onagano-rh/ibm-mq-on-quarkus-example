# IBM MQ on Quarkus Example

## 使い方

使い方を示すための簡単なサンプルプロジェクトを用意した。
まずテスト用のMQサーバーをDockerもしくはPodmanで起動しておく。

```
podman run   --env LICENSE=accept   --env MQ_QMGR_NAME=QM1   --publish 1414:1414   --publish 9443:9443   \
  --detach   --name ibmmq -e MQ_ADMIN_PASSWORD=admin -e MQ_APP_PASSWORD=app icr.io/ibm-messaging/mq:9.1.5.0-r2
```

バージョンは 9.1.5.0-r2 から 9.4.5.0-r1 が用意されている（ `skopeo list-tags docker://icr.io/ibm-messaging/mq` で確認できる）ようだが、
特殊な機能を使っていない限りMQクライアントの互換性により基本的にどのバージョンでも動く。
すでにテスト用のMQサーバーがある場合は [application.properties](src/main/resources/application.properties) に接続情報を設定しておくこと。

そしてサンプルプロジェクトを動かす。

```
git clone https://github.com/onagano-rh/ibm-mq-on-quarkus-example
cd ibm-mq-on-quarkus-example
./mvnw quarkus:dev
```

## 同期的な送受信

/hello/send のエンドポイントで DEV.QUEUE.1 に特定のJSONメッセージを送信する。
それを /hello/receive のエンドポイントで受信するように [GreetingResource.java](src/main/java/org/acme/GreetingResource.java) でコーディングしている。

```
$ curl http://localhost:8080/hello/send
{"message":"Sent to queue.1","timestamp":1771218164556}
$ curl http://localhost:8080/hello/receive
{"message":"Sent to queue.1","timestamp":1771218164556}
```

## 非同期的な受信

/hello/send2 のエンドポイントで DEV.QUEUE.2 に特定のJSONメッセージを送信する。
こちらはMDBの代わりとして使える [MyMessageListener.java](src/main/java/org/acme/jmslistener/MyMessageListener.java) で自動的に受信される。

```
$ curl http://localhost:8080/hello/send2
{"message":"Sent to queue.2","timestamp":1771218543781} 
# 即座に自動受信されQuarkusの標準出力に同じメッセージが出力される。
```

### メッセージリスナーの停止と再開

MDB相等のメッセージリスナーを安全に停止・再開するためのテスト用のエンドポイント /hello/start-listener と /hello/stop-listener を用意している。

```
# メッセージリスナーの停止
$ curl http://localhost:8080/hello/stop-listener
Listener stopped

# 停止中にいくつかメッセージを溜めておく
$ curl http://localhost:8080/hello/send2
$ curl http://localhost:8080/hello/send2

# メッセージリスナーの再開
$ curl http://localhost:8080/hello/start-listener
Listener started
# Quarkusのログで受信再開を確認できる
```

こうしたメッセージリスナーの管理は [MessageListenerManager.java](src/main/java/org/acme/jmslistener/MessageListenerManager.java) で行っている。

## RESTクライアントの例

その他、RESTクライアントの例として /hello/call-rest を呼び出すと
（自分のサーバーの） /hello を呼び出すようになっている。

```
$ curl http://localhost:8080/hello/call-rest
Hello from Quarkus REST
```

## 参考リンク

- https://github.com/ibm-messaging/mq-container
- https://ja.quarkus.io/version/3.20/guides/rest-client
