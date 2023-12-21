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
