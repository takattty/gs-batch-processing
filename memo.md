# ログから見る動き方（chunk size: 3）
1. readerの呼び出し
2. processorの呼び出し
3. writerの呼び出し
4. step1の呼び出し
5. importUserJobの呼び出し
6. 5回分Convertingとprocessの実行
7. 5回文のListenerのログ出力

SimpleJobLauncherの部分からJobが開始され、多分だけど@Beanが順番に読み込まれているだけっぽい？
あ、でも先にstepが呼ばれてJobが呼ばれてるな。
Jobでstep使うから先に用意する的な意味かな。

でその後にprocessが呼ばれる。
"start processor"が呼ばれたのは1回だけど、"start process"は5回なので、Beanとして用意する事と中の処理は別で考えた方がいい？
具体例が少ないから、あまり予測できないな。
他のreaderとかwriterは同じように1回しか呼ばれてないイメージ？
ただ処理が複雑になるとBatchConfigに書くの大変だろうから、readerもwriterも別クラスにするだろうな。

どこで5回呼び出す事を判定してるんだろう。CSV読み込んで行なかったらAbstractStepが呼ばれて、Job完了になってる。
nullがあったらどうなるんだろう。
```log
start process
2023-12-21T23:53:59.126+09:00  INFO 18427 --- [           main] c.e.batchprocessing.PersonItemProcessor  : Converting (Person[firstName=null, lastName= null]) into (Person[firstName=NULL, lastName= NULL])
start process
2023-12-21T23:53:59.127+09:00  INFO 18427 --- [           main] c.e.batchprocessing.PersonItemProcessor  : Converting (Person[firstName=taro, lastName= yamada]) into (Person[firstName=TARO, lastName= YAMADA])
```
関係なかったわ。readerでnullを返したい。
```log
start process
toStoring : Person[firstName=null, lastName=null]
transformedPerson : null
null
start process
toStoring : Person[firstName=taro, lastName=yamada]
transformedPerson : null
2023-12-22T00:13:33.000+09:00  INFO 18676 --- [           main] c.e.batchprocessing.PersonItemProcessor  : Converting (Person[firstName=taro, lastName=yamada]) into (Person[firstName=TARO, lastName=YAMADA])
Person[firstName=TARO, lastName=YAMADA]
```
あれ、null返せてるのに止まらないな。
aaaaaaaaaaa、processでnull返しても意味ないわな。readerでどうやって返すかわからんので、次の機会に。
あとチャンクサイズ変えて何が変わったかわからん。
> Chunkでは、コミット間隔に指定した件数だけReader、Processorを実行します。その後、Writerを1回実行
田村達也. 悲惨なミスをなくすSpringBatch入門書: Spring解体新書(バッチ編): 基礎から学べるSpring Batch (p.69). Kindle 版. 

らしい。今の書き方じゃあまりわからんから、別のサンプル作ろう。