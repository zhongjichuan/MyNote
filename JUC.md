1. Runnable是接口，其中定义了一个run()的抽象方法，Thread实现了Runnable,并对其扩展。
2. ExecutorService是Java中对线程池定义的一个接口，可以帮助我们很方便的创建各种类型ExecutorService线程池。最终创建的都是ThreadPoolExecutor，只是每种线程池的参数不一样
    - newCachedThreadPool 创建一个可缓存线程池，可以回收空闲线程，将其进行复用；corePoolSize被设置为0，没有空闲线程则新建线程。适用于执行很多的短期异步任务的小程序。
    - newFixedThreadPool 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。
    - newScheduledThreadPool 创建一个定长线程池，支持定时及周期性任务执行。
    - newSingleThreadExecutor 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行，适用于需要保证顺序地执行各个任务。  
CachedThreadPool 和 ScheduledThreadPool允许的创建线程数量为 Integer.MAX_VALUE； FixedThreadPool 和 SingleThreadPool允许的请求队列长度为 Integer.MAX_VALUE 。 
submit和execute区别：
- execute只能接受Runnable类型的任务；submit可以接受Runnable和Callable
- execute没有返回值； submit有返回值Future
- execute抛出异常需要通过UncaughtExceptionHande回调捕捉异常；submit通过调用future.get()对其进行try catch来捕获异常。

3. 线程生命周期：新建，就绪，运行，阻塞（一个线程在执行过程中暂停），销毁
![image](https://img2018.cnblogs.com/blog/1223046/201907/1223046-20190722214114154-276488899.png)
4. 线程的业务执行逻辑在run()方法中，通过start方法启动线程。Thread的run()和start()的实现为**模板设计模式**，父类编写算法结构，子类实现逻辑细节，即往不变的模板中填充代码逻辑。
5. 实现runnable接口的线程可以实现资源共享，而Thread不可以。Thread初始化init时，会继承线程的局部变量的默认值。  
有歧义，Thread是创建三个独立的对象，每个对象中都有自己的资源；runnable的实现类被三个Thread共用。
6. 堆内存是JVM中最大的一块内存区域，被所有的线程所共享，而栈内存和每个线程一一对应，一个进程的内存粗略大小 = 堆内存+线程数量 * 栈内存
7. 守护线程是一种特殊的线程，在后台默默完成一些系统性的服务，比如垃圾回收线程，JIT(即时编译)线程就可以理解为守护线程。相对的是用户线程。
8. **yield**会提醒调度器我愿意放弃当前的CPU资源，使当前线程从running状态切换至runnable状态。如果CPU资源不紧张，则会忽略这种提醒。这方式是会导致线程上下文的切换，但也不一定起效。
9. 线程处于阻塞状态时，```Thread.interrupt()```将中断标志设置为 true并抛出一个InterruptedException异常，在```catch{}```中做业务处理，会打断线程的阻塞状态。**wait**(),**sleep**(),**join**(),Selector.wakeup()等都会使线程进入阻塞状态。  
如果线程未处于阻塞状态，只是将中断标志设置为true，可以使用```Thread.interrupted()```检测线程中断标志做业务处理。  
Thread.interrupted()的作用是判断是否被中断，并清除标志位。
10. **Thread1.join**()会使**当前线程**进入等待，直到目标线程**Thread1**执行结束，或者当前线程被interrupt()中断，当前线程才会继续执行下去。join()方法的本质是调用了wait()方法实现线程的阻塞。
11. **synchronized**关键字提供互斥机制，可以保证被它修饰的方法或者代码块在同一时刻只有一个线程执行。  
synchronized关键字经过Javac编译后，会在同步块的前后分别形成monitorenter和monitorexit字节码指令。这两个字节码指令都需要一个reference类型的参数来指明要锁定和解锁的对象。
12. 什么是**Monitor**？
    - Monitor是一种用来实现同步的工具,==一个monitor的lock的锁只能被一个线程在同一时间获得==
    - 与每个java对象相关联，即每个java对象都有一个Monitor与之对应
    - Monitor是实现Sychronized(内置锁)的基础
13. **wait**()和**notify**()并不是Thread()特有的方法，而是Object中的方法。
    - **wait**()方法会导致当前线程进入阻塞，直到有其他线程调用notify()或notifyAll()方法才能将其唤醒。 
    - **notify**()方法会唤醒单个正在这行该对象wait()方法的线程，被唤醒的线程需要重新获取该对象所关联monitor的lock才能继续执行。
    - 必须在**同步方法**中使用wait()和notify()，因为执行wait()和notify()的前提条件是持有同步方法的monitor的所有权。
14. **synchronized**关键字无法控制阻塞时长；且==阻塞不可被中断，即interrupt()无法中断==。可以++自定义显示锁++，使其在具备synchronized关键字所有功能的同时又具备可中断和lock超时的功能。
15. **wait**()和**sleep**()的比较：
    - wait和sleep方法都可以使线程==进入阻塞状态，都是可中断方法==，可以被interrupt方法中断，抛出中断异常。
    - wait是Object的方法，而sleep是Thread的特有方法。
    - ==wait必须在同步方法中进行==，即代码要在synchronized中，而sleep不需要。
    - sleep不会释放monitor的锁，而wait会释放是monitor的锁，被唤醒后重新获取monitor的锁。
    - sleep方法会在短暂休眠后主动退出阻塞，而wait方法则需要被其他线程中断后才能退出阻塞。
16. 创建线程的时候如果没有显示地指定**ThreadGroup**，那么新的线程会被加入与父线程相同的ThreadGroup中。默认情况，新线程都会被加入到main线程的group中。ThreadGroup并不是用来管理Thread，而是针对Thread的一个组织，进行操作。
17. 线程在执行单元中是不允许抛出checked异常的（run()中不能抛出异常），线程运行在自己的上下文中，派生他的线程无法直接获得他运行中出现的异常信息。  
Java提供**UncaughtExceptionHandler**接口，当线程在运行过程中出现异常时，回调 UncaughtExceptionHandler接口，从而得知是哪个线程在运行时出错，以及出现什么错误。
```
thread-1.setUncaughtExceptionHandler((thread, e) ->{
            System.out.println(thread.getName());
            System.out.println(e);
        });
```
18. 线程池**ThreadPoolExecutor**主要参数：  
- corePoolSize 线程池常驻线程大小，即线程池空闲时最小线程数量
- maximumPoolSize 线程池最大线程数量，新的任务被创建如果没有空闲的核心线程，该任务会被加入到队列中，==队列满了时，才会创建新的线程去执行队列中的任务==。
- keepAliveTime 空闲线程存活时间，一个线程如果处于空闲状态，并且当前的线程数量大于corePoolSize，那么在指定时间后，这个空闲线程会被销毁
- workQueue corePoolSize达到上限，新任务会先进入到此工作队列中；队列满了会新建线程，从队列中取出任务去执行，上限为maximumPoolSize；
    - ArrayBlockingQueue，基于数组的有界阻塞队列，按FIFO排序
    - LinkedBlockingQueue，基于链表的阻塞队列，可以设置长度，最大0x7fffffff（视为无边界），按照FIFO排序
    - SynchronousQueue，一个不缓存任务的阻塞队列，即有新任务就新建线程；线程数量上限则执行拒绝策略
    - PriorityBlockingQueue，具有优先级的无界阻塞队列，优先级通过参数Comparator实现
- threadFactory 线程工厂，创建一个新线程时使用的工厂，可以用来设定线程名
- handler 拒绝策略，当工作队列中的任务已到达最大限制，并且线程池中的线程数量也达到最大限制，这时如果有新任务提交进来，根据对应的策略进行处理  
    - CallerRunsPolicy，调用者线程直接执行被拒绝任务的run方法，因此调用者线程会被阻塞
    - AbortPolicy，直接丢弃任务，并抛出RejectedExecutionException异常
    - DiscardPolicy，直接丢弃任务，什么都不做
    - DiscardOldestPolicy，抛弃进入队列最早的那个任务，然后尝试把这次拒绝的任务放入队列  
    
[博客参考](https://blog.csdn.net/ye17186/article/details/89467919)  
**合理配置线程池**  
CPU密集型，配置尽可能小的线程，CPU核数+1个，任务需要大量运算,没有阻塞,CPU一致运行；  
IO密集型，配置尽可能多的线程，CPU核数 * 2，线程不是一直执行任务,可以多分配一些线程数。  
```
//获取CPU核数
Runtime.getRuntime().availableProcessors()
```

****
19.   
   ![image](https://img-blog.csdnimg.cn/20201121203257325.png?x-oss-process=image)
20. JVM对类的初始化是一个延迟的机制，当一个类在首次使用时才会被初始化。类的被动使用不会导致类的加载和初始化，如构造某个类的数组，引用类的静态final常量。
21. **类的加载过程**  
- 加载阶段，将class文件中的二进制数据读取到内存之中，然后将该字节流所代表的静态存储结构转换为方法区中运行时的数据结构，并在堆内存中生成一个代表该类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口。
- 连接阶段
    - 验证，确保被加载类的正确性，字节流是否符合JVM规范
    - 准备，为该对象的静态变量分配内存，并设置默认值，比如int默认值为0
    - 解析，将常量池中的符号引用替换为直接引用，针对该对象中引用的类接口、字段、类方法和接口方法进行解析。在编译的时候一个每个java类都会被编译成一个class文件，但在编译的时候虚拟机并不知道所引用类的地址，所以就用符号引用来代替，而在解析阶段就是为了把这个符号引用转化成真正的地址的阶段。
- 初始化阶段，为类中的变量赋予正确的值，会执行所有类变量的赋值动作和静态语句块。先父后子，先静态，再代码块，后构造。  
PS：静态代码块会在类第一次被加载到内存中时执行，所以++静态代码块只会被执行一次++。无论被new()多少次。
22. **双亲委托机制**，当一个类加载器被调用了loadClass后，它并不会直接将其加载，而是先交给当前类加载器的父加载器尝试加载直到最顶层的父加载器，++然后再依次向下进行加载++。**打破双亲委派机制**则要继承ClassLoader类，重写loadClass()方法，不去加载父类加载器。  
**沙箱机制**将 Java 代码限定在虚拟机(JVM)特定的运行范围中，并且严格限制代码对本地系统资源访问，通过这样的措施来保证对代码的有效隔离，防止对本地系统造成破坏。
![image](https://img-blog.csdnimg.cn/20201121203910562.png?x-oss-process=image)
23. 
- ClassLoader.loadClass()并不会导致类进行初始化，只是执行了加载过程中的加载阶段。  
- Class.forName()会加载类，并且会初始化，执行类中的静态代码块以及对静态表里的赋值。在编写JDBC代码时，都需要调用Class.forName("xxx.xxx.Driver")，就是为了在静态代码块中将数据库的Driver实例注册给DriverManager。
****
24. **volatile**的出现原因
- 为了应对CPU越来越高的计算速度，CPU与主存之间增加了CPU Cache，会先修改CPU cache再同步至主存中，但在多线程情况下可能造成数据不一致。CPU运算速率远远大于内存的读写频率。CPU3.7GHz，内存条两千MHz，甚至才几百MHz。
![image](https://images0.cnblogs.com/blog/288799/201408/212219343783699.jpg)  
- ==缓存一致性协议==: 
    - 含义：CUP会有一个时刻监听（++总线嗅探机制++），当某块CPU对缓存中的数据进行写操作之后，就通知其他CPU放弃存储在它们内存中的缓存，使用时再从主存中获取。
    - MESI协议：是以缓存行（CPU的最小缓存单位）的几个状态来命名的。（Modified，Exclusive,Share,Invalid） 
    - 这是需要遵循的协议，内存屏障是对该协议的实现
>       M：被修改的。处于这一状态的数据，只在本CPU中有缓存数据，而其他CPU中没有。同时其状态相对于内存中的值来说，是已经被修改的，且没有更新到内存中。
   >     E：独占的。处于这一状态的数据，只有在本CPU中有缓存，且其数据没有修改，即与内存中一致
   >     S：共享的。处于这一状态的数据在多个CPU中都有缓存，且与内存一致。
   >     I：无效的。本CPU中的这份缓存已经无效。
  >  假设主内存中有一个变量x=1，当cpu1读取了之后，cpu1中就会有缓存变量x(E)就会有一个时刻监听(++总线嗅探机制++)去监听主        内存，这时候，加入cpu2也读取了，那么cpu1中就会变成x(S),而cpu2中也会存在x(S)，cpu2也会对主内存监听，这时候，当      cpu1对x做了修改，那么cpu1中就变成了X(M),当cpu通知了主内存之后，cpu1中就变成了X(E),而cpu2中则变成了X(I),于是         cpu2中的x就被抛弃，这时候会重新获取，重新获取之后，cpu1和2中都会变成X(S)
        
    - 两个线程同时修改？不会的，在一个指令周期内，系统执行指令时，会进行裁决，裁决胜利的就可以修改。
    - MESI失效场景：当数据的长度大于CPU中的缓存行，会跨域多个缓存行，因此缓存行状态不能被正确标识，此时会使用总线加锁。
- 总线锁机制：CPU1对变量做修改时，会通过总线发出#LOCK信号，其他CPU就不能操作该变量，即锁住变量的内存区域，造成其他CPU阻塞。
- java内存模型（JMM）是一个抽象的概念，指定了JVM如何和计算机主存进行工作。每个线程都有自己的工作内存，线程不能直接操作主存，只有先操作了工作内存才能写入主存。这样也会存在数据不一致的问题。
25. 并发编程的三大特性：原子性，可见性，有序性
- 原子性，指操作是++不会受到任何因素的干扰而中断++，要么一起执行完成，要么不执行。synchronized可以保证原子性
- 可见性，就是指当一个线程修改了共享变量的值时，其他线程能够立即得知这个修改。++**volatile**修饰的变量，修改后能立即同步回主内存，以及每次使用前会直接从主内存读取++；**synchronized**也能够保证可见性，线程在释放锁之前，会把共享变量值都刷回主存。
- 有序性，在Java内存模型中，为了提高性能，允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。volatile，synchronized都能保证代码的有序性。
26. **volatile**关键字的语义：
- 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，使其他线程工作内存中的数据失效（具体为CPU缓存行失效）。如果需要重新读取该变量，会重新从主存中读取，通过这种方式使新值对其他线程来说是立即可见的。通过**内存屏障指令**实现。
- 禁止进行指令重排序，对被volatile修饰的变量的写操作要早于之后对该变量的读操作。禁止JVM和CPU对被volatile修饰的变量进行指令重排。java编译器会在生成指令系列时在适当的位置会插入**内存屏障**指令来禁止特定类型的处理器重排序，处理器在重排序时不能把后面的指令重排序到内存屏障之前的位置。
- volatile无法保证原子性，所以volatile是线程不安全的。++在执行字节码指令时，多个线程的会并发执行字节码指令，造成变量的值不可控++。如两个线程从主内存中读取到的值都是a=1，a++后在写入主内存中。
27. ==**volatile**和**synchronized**区别==
    - volatile只能修饰实例变量或类变量；synchronized只能修饰方法或者语句块。
    - volatile修饰的变量可以null，synchronized同步语句块的monitor对象不能为null。
    - volatile无法保证原子性，synchronized可以保证原子性。
    - 两者都可以保证共享资源在多线程间的可见性，但实现机制不同。==synchronized借助JVM指令monitor exit,使共享资源刷新至主存中；volatile使用的则是机器指令“lock”(内存屏障，CUP的一个指令)，使其他线程工作内存中的数据失效，不得已到主存中进行再次加载。==
    - 两者都保证了有序性。
    - volatile不会使线程陷入阻塞；synchronized会使线程进入阻塞状态。
28. **ThreadLocal**，访问ThreadLocal变量的每个线程都会有这个变量的一个本地拷贝，多个线程操作这个变量时，实际操作的是自己本地内存中变量，从而规避了线程访问共享资源的安全问题。
> ThreadLocal存放变量，变量对于每个线程都是惟一的，并且可以从线程的所有执行路径访问。 

 **InheritableThreadLocal**，子线程中可以获取到父线程的InheritableThreadLocal变量。  

```
static ThreadLocal<String> localVar = new ThreadLocal<>();
new Thread(() -> localVar.set("123")).start();
```
ThreadLocal中有个ThreadLocalMap，以ThreadLocal的弱引用作为key。ThreadLocal没有外部强引用，那么在下次垃圾回收的时候会被清理掉，Map中就存在(null,value)的情况。线程若一直存在，则这个Map也就一直存在，导致(null,value)所涉及的对应一直得不到回收，发生内存泄漏。
[Thread内存泄漏原因](https://blog.csdn.net/Rex_WUST/article/details/98959422)
> 主线程结束，request对象会被销毁，此时子线程中的request获取属性会返回null
29. 线程runnable和callable的区别
- Callable规定的方法是call(),Runnable规定的方法是run().
- Callable的任务==执行后可以有返回值==，得到一个**Future**对象，而Runnable的任务是不能返回值
- ==call方法可以抛出异常==，run方法不可以
30. FutureTask简单示例：
```
// 异步查询商品规格
FutureTask<List<GoodsNameAndSpecDetail>> futureTask = new FutureTask<>(
    new Callable<List<GoodsNameAndSpecDetail>>() {
        @Override
        public List<GoodsNameAndSpecDetail> call(){
            return goodsInfoSpecDetailRelMapper.findGoodsNameAndSpecDetail(skuIds);
        }
    }
);
futureTasks.add(futureTask);
new Thread(futureTask).start();
futureTasks.forEach(v -> {
    try {
        nameAndSpecDetails.addAll(v.get()); //获取异步结果
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    }
});
```
31. synchronized和lock异同：
- Lock需要手动释放，否则会造成死锁现象
- Lock是一个接口，synchronized是java关键字
- synchronized是非公平锁，Lock默认是非公平锁，也可以设置公平锁
- synchronized和lock都可以保证原子性

```
class X {
   private final Lock lock = new ReentrantLock();
   public void m() {
     //获取了锁立即返回，如果别的线程持有锁，当前线程则一直处于休眠状态，直到获取锁
     lock.lock(); 
     try {
       // ... method body
     } finally {
       lock.unlock()
     }
   }
 }
```
> **公平锁**是指多个线程按照申请锁的顺序来获取锁。**非公平锁**是指多个线程获取锁的顺序并不是按照申请锁的顺序，根据线程优先级来获取  

32. **JUC**是JDK1.5提供了并发编程的工具包java.util .concurrent的简称。  
**CAS**(Compare-And-Swap)可以保证++原子性++，CAS包含3个操作数：  
需要读写的内存值: V; 进行比较的预估值: A; 拟写入的更新值: B  
CAS算法是这样处理的：==当且仅当 V == A 时, 将B写进主存即V = B； 否则,重新从内存取值==  
CAS会导致ABA问题（增加标志位或者版本号避免ABA问题）；线程不停的重试会长时间占用CPU  
**AQS**,AbstractQueuedSynchronizer，可以叫做队列同步器，是JUC中极为重要的基类。使用锁后会导致线程阻塞，很多线程会处于阻塞等待状态，AQS提供了一种机制，可以唤醒阻塞队列并分配锁。
![image](https://img-blog.csdnimg.cn/20190203234437159.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N0cml2ZWI=,size_16,color_FFFFFF,t_70)  
AQS内部维护了一个CLH队列来管理锁。线程会首先尝试获取锁，如果失败就将当前线程及等待状态等信息包装成一个node节点加入到同步队列sync queue里。当前节点head释放资源后，后继节点再自旋获取锁。如果失败就会阻塞自己直到自己被唤醒。而当持有锁的线程释放锁的时候，会唤醒队列中的后继线程。
33. **volatile**修饰的变量如果是对象或数组之类的，其含义是对象或数组的地址具有可见性，==但是数组或对象内部的成员改变不具备可见性==  
34. **CountDownLatch**目的是让一个或者多个线程等待，直到其他线程的一系列操作完成。

```
private void test(){
    CountDownLatch countDownLatch = new CountDownLatch(6);
    for(int i = 0; i < 6; i++){
        new Thread(() -> {
            System.out.println(i);
            countDownLatch.countDown();
        }).start();
    }
    countDownLatch.await();//当前线程会阻塞，直到countDownLatch中count为0
    System.out.println("6个子线程全部完成后才执行");
}
```
35. **CyclicBarrier**一批线程之间相互等待各自达到共同的栅栏点，任何一个线程到达之前，所有的线程必须等待.更像是一道屏障，**只有所有线程到齐之后**才能解除这道屏障。

```
private void test(){
    CyclicBarrier cyclicBarrier = new CyclicBarrier(7, 
        () -> { System.out.println("7个线程全部执行完"); }
    );
    for(int i = 0; i < 7; i++){
        new Thread(() -> {
            System.out.println(i);
            //直到7个线程执行到这里，执行完cyclicBarrier中的方法，才继续往下
            cyclicBarrier.await();
        }).start();
    }
}
```
36.@Async的是默认的实现SimpleAsyncTaskExecutor，这不是个真的线程池，他不会重用线程，默认每次都重新创建一个新的线程，导致每次都有创建、销毁线程的开销。 
```
  protected void doExecute(Runnable task) {
        Thread thread = this.threadFactory != null ? this.threadFactory.newThread(task) : this.createThread(task);
        thread.start();
    }
```
推荐实现AsyncConfigurer接口```public class AsyncConfig implements AsyncConfigurer```，或指定自定义的TaskExecutor
37. ThreadLocal博客参考：https://blog.csdn.net/u010445301/article/details/111322569  
https://blog.csdn.net/qq_38293564/article/details/80459827
38. J.U.C是Java的另一种全新的互斥同步手段，基于Lock接口，用户能够以**非块结构**来实现互斥同步。  
重入锁**ReentrantLock**是Lock接口最常见的一种实现，主要要三项功能：  
    1. 等待可中断：当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。
    2. 公平锁：多个线程在等待同一个锁时，必须按照申请锁的时间顺序来一次获得锁。但是公平锁会导致性能急剧下降，会明显影响吞吐量。synchronized是非公平锁，ReentrantLock默认是非公平锁。
    3. 锁绑定多个条件
39. 

---
# Java并发编程的艺术
## 第一章 并发编程的挑战
#### 1.1 上下文切换
CPU通过时间片分配算法来循环执行任务，切换任务前会保存上一个任务的状态，以便下次切换回这个任务时，可以再加载这个任务的状态。所以任务从保存到再加载的过程就是一次上下文切换。  
#### 1.2 如何减少上下文切换？
1. 无锁并发编程。多线程竞争锁，会造成线程阻塞，导致频繁的上下文切换，需要尽量减少锁的竞争或避免使用锁。比如将数据的ID按照Hash算法取模分段，不同的线程处理不同段的数据。
2. CAS算法
3. 使用最少线程，避免创建不需要的线程
4. 协程：在单线程里实现多任务的调度，并在单线程里维持多个任务间的切换。
#### 1.3 并发的优缺点
- 优点：充分利用多核CPU的计算能力；方便进行业务拆分，如生成订单和发送邮件，提升应用性能。
- 缺点：高并发场景下会导致频繁的上下文切换；临界区线程安全问题，容易出现死锁，产生死锁造成系统功能不可用。
#### 1.4 内核空间，用户空间




## 第二章 Java并发机制的底层实现原理
#### 2.1 volatile的原理
有volatile变量修饰的共享变量进行写操作的时候，会增加Lock#指令。  
- Lock#指令会将当前处理器缓存行的数据写回到系统内存；
- 这个写内存的操作会使在其他CPU里缓存了该内存地址的数据无效。通过**缓存一致性协议MESI**，每个处理器通过**嗅探**在总线上传播的数据来检查自己缓存行中的对应的内存地址是否被修改。若被修改，就会将当前处理器的缓存行设置成无效状态。当处理器对这个数据进行修改操作是，会重新从系统内存中把数据读到处理器缓存里。
#### 2.2 synchronized的原理
synchronized实现同步的基础：Java中的每一个对象都可以作为锁。
- 对于普通同步方法，锁是当前实例对象。
- 对于==静态同步方法==，琐是当前类的Class对象。
- 对于同步方法块，锁是Synchronized括号里配置的对象。
JVM基于进入和退出Monitor对象来实现方法同步和代码块同步。代码块同步是使用```monitorenter```和```monitorexit```指令实现的。代码在编译后，两个指令分别插入到同步代码块的开始位置和结束位置。
##### 2.2.1 手动加锁，解锁
```
UnsafeInstance.reflectGetUnsafe().monitorEnter(object);
UnsafeInstance.reflectGetUnsafe().monitorExit(object);
```
##### 2.2.2 底层原理
synchronized是基于JVM内置锁实现，通过内部对象Monitor(监视器锁)实现，基于进入与退出Monitor对象实现方法与代码块同步，++监视器锁的实现依赖底层操作系统的Mutex lock（互斥锁）实现，它是一个重量级锁性能较低++。  
```synchronized```保证了对进入同一监视器的线程保证可见性。
> JMM关于synchronized的两条规定： 
> 1. 线程解锁前，必须把共享变量的最新值刷新到主内存中
> 2. 线程加锁时，将清空工作内存中共享变量的值，从而使用共享变量时需要从主内存中重新读取最新的值（注意：加锁与解锁需是同一把锁）

#### 2.3 锁优化
##### 2.3.1 锁的升级
JDK1.6 对锁的实现引入了大量的优化，如偏向锁、轻量级锁、自旋锁、适应性自旋锁、锁消除、锁粗化等技术来减少锁操作的开销。
##### 2.3.2 自旋锁、偏向锁、轻量级锁
- **自旋锁**：++互斥同步会使线程挂起，后续再恢复，会对处理器的并发性能造成很大的压力++。而且通常情况，线程持有锁的时间较短，为了这段时间去挂起和恢复线程并不值得。为了不放弃处理器的执行时间，让线程执行一个空循环（自旋）。如果若干次（自旋次数可以设置）仍不能获取锁，再将线程挂起。
> 自适应自旋：JVM会由前一次在同一个锁上的自旋时间及锁的拥有者的状态决定本次自旋情况。如果上次线程通过自旋获得了锁，那么本次甚至可能会允许自旋等待更长的时间；如果上次有没获取到锁，那么以后在获取锁时可能会直接略过自旋过程，以免浪费处理器资源。
- **偏向锁**：如果一个线程获得了锁，那么锁就进入偏向模式，此时Mark Word的结构也变为偏向锁结构，会在对象头和栈帧中的锁记录里存储偏向锁的++线程ID++。当这个线程再次获取锁，无需再次做任何同步操作，这样就省去加锁、解锁的操作。一旦出现另外一个线程去尝试获取这个锁，偏向模式立马结束，会升级为轻量级锁。
> 当一个对象已经计算过identityhashcode，它就无法进入偏向锁状态；当一个对象当前正处于偏向锁状态，并且需要计算其identity hashcode的话，则它的偏向锁会被撤销，原本存放线程ID等区域将用来存放hashCode,并且锁会膨胀为重量级锁。  
> 轻量级锁会在锁记录Lock Record中记录hashCode；重量级锁会在Monitor中记录 hashCode。
- **轻量级锁**：设计的初衷是在++没有++多线程竞争的前提下，减少系统的重量级锁（monitor锁）使用操作系统互斥量产生的性能消耗。即判定是否有其他线程争夺当前对象的锁，有则升级为重量锁，反之不需要。  
##### 2.3.3 Mark Word和Monitor
###### Mark Word
![image](https://img-blog.csdnimg.cn/5d6b32a7b98843249d839ed2754585e5.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5Luy5bCP5bed,size_20,color_FFFFFF,t_70,g_se,x_16)
###### Monitor
![image](https://img-blog.csdnimg.cn/20210130135025351.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl81MDI4MDU3Ng==,size_16,color_FFFFFF,t_70)  
1. 刚开始时 Monitor 中的 Owner 为 null
2. 当 Thread-2 执行 synchronized(obj){} 代码时就会将 Monitor 的所有者Owner 设置为 Thread-2，上锁成功，Monitor 中同一时刻只能有一个 Owner
3. 当 Thread-2 占据锁时，如果线程 Thread-3 ，Thread-4 也来执行synchronized(obj){} 代码，就会进入 EntryList（阻塞队列） 中变成BLOCKED（阻塞） 状态
4. Thread-2 执行完同步代码块的内容，然后唤醒 EntryList 中等待的线程来竞争锁，竞争时是非公平的
5. WaitSet 中的 Thread-0，Thread-1 是之前获得过锁，但条件不满足进入 WAITING 状态的线程
6. 对象被```synchronized```作用才会关联到Monitor
> **加锁过程**：在代码即将进入同步块的时候，如果对象的锁标志没有被锁定（01），虚拟机首相将在当前线程的栈帧中建立一个锁记录（Lock Record）的空间，用于存储锁对象目前Mark Word的拷贝。然后虚拟机将使用CAS将对象头中的Mark Word的锁指针替换为指向锁记录的指针。如果成功，当前线程获得锁，++如果失败，表示有其他线程竞争锁，此时轻量级锁需要**膨胀**为重量级锁++，为当前对象申请monitor锁，让对象指向monitor，当前线程则加入monitor中的EntryList。
##### 2.3.3 锁的消除
锁消除是指虚拟机**即时编译器JIT**在运行时检测到某段需要同步的代码根本++不可能存在共享数据竞争++而实施的一种对锁进行消除的优化策略。锁消除的主要判断依据来源于**逃逸分析**。
##### 2.3.4 锁的粗化
```
public String concatString(String s1, String s2, String s3){
    StringBuffer sb = new StringBuffer();
    sb.append(s1);      // StringBuffer.append()是一个同步块
    sb.append(s2);
    sb.append(s3);
    return sb.toString();
}
```
一系列的操作都对同一个对象反复加锁和解锁，即使没有线程竞争，这样也会导致不必要的性能损耗。JVM探测到有这样的场景，将会进行锁粗化，将锁同步的范围扩展到整个操作序列外围。
#### 2.4 ==原子操作==的实现原理
原子操作，不可被中断的一个或一系列操作。
##### 处理器实现原子操作的原理
1. 使用总线锁保证原子性  
处理器提供了一个LOCK#信号，当一个处理器在总线上输出此信号时，其他处理器的请求将被阻塞住，那么该处理器此时便独占共享内存。
2. 缓存一致性协议  
频繁使用的内存会缓存在处理器的L1,L2,L3高速缓存里，可以很好的解决处理器与内存的速度矛盾，但是也会引入新的问题：缓存一致性。  
通过MESI协议使其他处理器中的缓存行无效。高速缓存中的数据会被“缓存锁定”，当数据被回写到主内存时，缓存一致性机制会使其他处理器中对应的缓存行无效。
当操作的数据不能被缓存在处理器内部，或操作的数据跨多个缓存行是时，处理器会调用总线锁定。
##### Java实现原子操作原理
1. 锁机制，锁机制保证了只有获得锁的线程才能够操作锁定的内存区域。JVM实现锁的方式都采用了循环CAS。
2. 使用循环CAS实现原子操作，通过循环进行CAS操作指导成功为止。
> CAS实现原子操作的三大问题：
> 1. ABA问题。解决思路：使用版本号。使用```AtomicStampedReference```，除了初始值，还有另一个```stamp```属性，用作版本记录。
> 2. 循环时间长开销大，自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。
> 3. 只能保证一个共享变量的原子操作。可以将多个共享变量合并放到一个对象中。
> 4. 如果线程数远多于CPU核数，会导致多余的线程阻塞，挂起。
## 第三章 Java内存模型
#### 3.1 Java内存模型的抽象结构
![image](https://img-blog.csdnimg.cn/2aec431f28444897bb7ded0d1fef3436.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5Luy5bCP5bed,size_20,color_FFFFFF,t_70,g_se,x_16)
#### 3.2 重排序
重排序是指编译器和处理器为了优化程序性能而对指令序列进行重新排序的一种手段。  
![image](https://img-blog.csdnimg.cn/f06e4b90f9bc41feaa1f5ac37c5ce450.png)
- 编译器优化重排序：编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。
- 指令级并行的重排序：处理器多条指令重叠执行，改变语句对应机器指令的执行顺序
> 重排序会导致多线程程序出现内存可见性问题。为了解决重排序问题，JMM会要求Java编译器在生成指令序列时，插入特定类型的内存屏障指令，通过内存屏障指令来禁止特定类型的处理器重排序。
##### 3.2.1 as-if-serial
as-if-serial语义：不管怎么重排序，单线程的执行结果不能被改变。
#### 3.3 volatile的内存语义
##### 3.3.1 内存语义
- 保证被volatile修饰的共享变量对所有线程总数可见的，也就是当一个线程修改了一
个被volatile修饰共享变量的值，新值总是可以被其他线程立即得知。
- 禁止指令重排序优化。
volatile保证了变量的可见性，程序的有序性，但无法保证原子性。
##### 3.3.2 内存语义的实现
volatile 的底层实现原理是内存屏障，Memory Barrier（Memory Fence）
- 对volatile变量的写指令后会加入写屏障
- 对volatile变量的读指令前会加入读屏障
###### 可见性
- 写内存屏障,在指令后插入```Store Barrier```,能让写入缓存中最新的数据更新写入主内存,让其他线程可见.强制写入主内存,这种显示调用,CPU就不会因为性能考虑而去对指令重排
- 读内存屏障,在指令前插入Load Barrier,可以让高速缓存中的数据失效,强制从新主内存中加载数据读取主内存内容,让CPU缓存与主内存保持一致,避免缓存导致的一致性问题
###### 有序性
JMM为了限制重排序，针对编译器制定的volatile重排序规则表。
![image](https://img-blog.csdnimg.cn/517c14fbb59d4b729da3d5475184f4ca.png)  
例如：在程序中，当第一个操作为普通变量的读或
写时，如果第二个操作为volatile写，则编译器不能重排序这两个操作。  
![image](https://www.freesion.com/images/320/f1948e4c350ebd7d2137ebdf4dea3728.png)
##### 3.3.3 总线风暴
如果在短时间内产生大量的CAS操作，或暴增式地触发volatile的嗅探机制则会不断地占用总线带宽，导致总线流量激增，就会产生总线风暴。 
##### 3.3.4 volatile的使用时机
volatile无法保重原子性，所以只能在一定条件下是线程安全的。（1）对变量的写操作不依赖于当前值；（2）该变量没有包含在具有其他变量的不变式中。
#### 3.4 锁的内存语义
加锁目的 ：序列化访问临界资源。
- [ ] 逃逸分析
- [ ] 锁的粗化，锁的消除
#### 3.5 final的内存语义
##### 3.5.1 final的作用
- final修饰的类不可被继承
- final修饰的引用在初始化后不可重新赋值
- final修饰的方法不可重写
##### 3.5.2 final的重排序
对于final域，编译器和处理器要遵守两个重排序规则：
1. 在构造函数内对一个final域的写入，与随后把这个被构造对象的引用赋值给一个引用变量，这两个操作不能重排序。先写入final变量，后调用该对象的引用。  
原理：编译器会在final域的写之后，插入一个StoreStore屏障
2. 初次读一个包含final域对象的引用，与随后初次读这个final域，这两个操作不能重排序。先读对象的引用，后读final变量。  
编译器会在读final域操作的前面插入LoadLoad屏障。  

通过内存屏障，保证了final所修饰变量的在多线程情况下访问安全

#### 3.6 happens-before
Happens-before规定了对共享变量的写操作对其他线程的读操作可见，它是可见性和有序性的一套规则的总结。
#### 3.7 双重检查锁定与延迟初始化
##### 3.7.1 基于volatile的解决方案
```
// 单例模式未使用volatile修饰存在的问题
public class DoubleCheckedLocking {                      //1
    private static Instance instance;                    //2
    public static Instance getInstance() {               //3
        if (instance == null) {                          //4: 第一次检查 
            synchronized (DoubleCheckedLocking.class) {  //5: 加锁 
                if (instance == null)                    //6: 第二次检查 
                    instance = new Instance();           //7: **问题的根源出在这里** 
            }                                            //8
        }                                                //9
        return instance;                                 //10
    }                                                    //11
} 
```

```
memory = allocate();   //1：分配对象的内存空间 
ctorInstance(memory);  //2：初始化对象 
instance = memory;     //3：设置 instance 指向刚分配的内存地址
```
2和3有可能重排序，多线程情况下，线程有可能访问到一个还未初始化的对象。  
++当声明对象```instance```的引用为```volatile```后，2和3将会禁止重排序。++
##### 3.7.2 基于类初始化的解决方案
在执行类的初始化期间，==JVM会去获取一个锁==。这个锁可以同步多个线程对同一个类的初始化。基于这个特性，可以实现另一种线程安全的延迟初始化方案。

```
public class InstanceFactory{
    private static class InstanceHolder{           
        private static Singleton instance = new Singleton();  //声明一个内部类     
    }           
    public static Singleton getInstance(){           
        return InstanceHolder.instance;  //导致内部类被初始化           
    } 
}
}

```


## 第四章 Java并发编程基础
#### 4.1 什么是线程
操作系统在运行一个程序时，会为其创建一个进程。例如启动一个Java程序，操作系统就会创建一个java进程。操作系统调度的最小单元是线程，在一个进程里可以创建多个线程。
#### 4.2 理解中断
中断可以理解为线程的一个标识位属性，仅表示一个运行中的线程是否被其它线程进行了中断操作。调用一个线程的interrupt()方法中断一个线程，并不是强行关闭这个线程，只是跟这个线程打个招呼，将线程的中断标志位置为true。
```
public class UserRunnable implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {//判断当前线程是否被中断
            System.out.println(Thread.currentThread().getName() + "is running");
        }
    }
}
```
> 1.如果线程处于阻塞状态会立马退出阻塞并抛出InterruptedException异常，线程可以通过捕获InterruptedException方法来做一定处理，然后让线程退出。  
> 2.如果线程处于运行中则不受任何影响继续运行，仅仅将线程的中断标记设置为true。
#### 4.3 等待/通知机制
线程通过```synchronized```获取对象的监视器Monitor失败，线程则会进入同步队列，线程状态变为BLOCKED(阻塞状态)。  
在同步块中使用```wait()```方法，线程状态由Running变为WAITING，并将当前线程放置到对象的等待队列。通过```notify()```方法可以将等待队列中的一个等待线程从等待队列中移到同步队列中，线程将重新尝试获取对象的锁。
![image](https://img-blog.csdnimg.cn/a0c2541404c64fcb86b9d42b2d294971.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5Luy5bCP5bed,size_20,color_FFFFFF,t_70,g_se,x_16)
#### 4.3 等待/通知的经典范式
```
// 等待方
synchronized(对象){
    while(条件不满足){
        对象.wait(); //不消耗处理器资源
    }
    业务逻辑
}

// 通知方
synchronized(对象){
    改变条件
    对象.notifyAll(); //及时通知
}
```
## 第五章 Java中的锁
#### 5.1 Lock接口
```synchronized```会隐式地获取锁，将锁的获取和释放固化了，缺乏可扩展性。相反，```Lock```则是显式地获取和释放锁。
```synchronized```和```Lock```区别：  
- synchronized没法响应中断，等待的线程将会一直阻塞下去；Lock可以响应中断。
- synchronized执行完同步代码块，或者发生异常，系统会自动释放锁；Lock必须在finally{}中释放锁。
- Lock是接口，有很多的实现类。可指定时间去获取锁，获取不到则返回。
#### 5.2 队列同步器AQS
##### 5.2.1 数据结构
![image](https://img-blog.csdnimg.cn/bcb3cb1fe2744ff08231f1bd7e636637.png)  
![image](https://pic1.zhimg.com/80/v2-1e7de5b46cca7428a430b14d3a503658_720w.jpg)
##### 5.2.2 AQS接口
AQS中维护了一个volatile int state（共享资源）和一个CLH队列。当state=1时代表当前对象锁已经被占用，其他线程来加锁时则会失败，失败的线程被放入一个FIFO的等待队列中，然后会被UNSAFE.park()操作挂起，等待已经获得锁的线程释放锁才能被唤醒。  

用state属性表示资源的状态(分独占模式和共享模式)，子类需要定义如何维护这个状态，控制如何获取锁和释放锁。
- getState():获取当前同步状态
- setState():设置state状态
- compareAndSetState():使用CAS设置当前状态，该方法能保证状态设置的原子性

```
//AQS获取锁的逻辑
while(state 状态不允许获取) {
    if(队列中还没有此线程) {
        入队并阻塞
    }
}
当前线程出队

//释放锁的逻辑
if(state 状态允许了) {
    恢复阻塞的线程(s)
}
```

##### 5.2.3 同步队列CLH
![image](https://upload-images.jianshu.io/upload_images/17369295-0b769bff719c44c4.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)  
线程在获取同步器的同步状态state```tryAcquire()```失败时，则构造同步节点Node并通过```addWaiter(Node node)```方法将该节点加入到同步队列的尾部。最后调用```acquire()```是的节点进入死循环的方式获取同步状态，如果节点的前节点不是头结点并且处于待唤醒状态，则将该节点挂起，避免死循环浪费资源。  
在释放同步状态时，同步器调用```tryRelease()```方法释放同步状态，然后唤醒头节点的后继节点。
#### 5.3 重入锁ReentrantLock
重入锁表示该锁能够支持一个线程对资源的重复加锁。此外，该锁还支持公平锁和非公平锁。  
##### 5.3.1 与```synchroinzed```的区别
- ```ReentrantLock```可中断
- 获取锁时可以设置超时时间
- 可以支持公平锁
- 支持多个条件变量
- 相同点：都支持可重入
##### 5.3.2 使用
不要将获取锁的过程写在try块中，因为如果在获取锁时发生了异常，异常抛出的同时，锁会无故释放。在finally块中再次解锁会抛异常```IllegalMonitorStateException```。
```
 Lock lock = new ReentrantLock();
 lock.lock();
 try {
   // access the resource protected by this lock
 } finally {
   lock.unlock();
 }
```
##### 5.3.3 原理


#### 5.4 读写锁ReentrantReadWriteLock
读写锁在同一时刻可以允许多个读线程访问，但是在写线程访问时，所有读线程和其他写线程均被阻塞。
#### 5.5 LockSupport工具
```LockSupport```定义了一组的公共静态方法，方法提供了最基本的线程阻塞和唤醒功能，```LockSupport```也成为了构建同步组件的基础工具。
```
LockSupport.park()
LockSupport.unpark();
```
#### 5.6 Condition接口
Condition是在Java1.5中才出现的，是AQS的内部类，用来替代传统的Object的wait()、notify()。
## 第六章 Java并发容器和框架
#### 6.1 ConcurrentHashMap
##### 6.1.1 HashMap死锁问题
在多线程场景下，使用hashMap进行put()操作会引起死循环。HashMap在多线程环境下，进行同时扩容，重新生成链表结构时可能会出现循环链表，即Entry的next节点永远不为空。这个在调用get()方法时就会产生死循环。  
[HashMap相关参考万米笔记.74](http://note.youdao.com/noteshare?id=21a04d1bb0a264f247ef5e67f53f0281&sub=7EB14FB9188C4370A14B404A366F359C)
##### 6.1.2 线程安全的ConcurrentHashMap
HashTable是线程安全的，但是性能差。JDK1.7中，ConcurrentHashMap采用的是分段锁，结构是多个Segment对象（继承了ReentrantLock），Segment对象中是n个HashEntry。每个Segment对象都有锁，一个线程访问时上锁，不影响其他线程访问其他Segment对象。
JDK1.8中，ConcurrentHashMap放弃了原有的Segment分段锁，采用CAS + synchronized实现更加细粒度的锁。https://blog.csdn.net/zycxnanwang/article/details/105424734
#### 6.2 阻塞队列BlockingQueue
阻塞队列是一个支持两个附加操作的队列，阻塞的插入和移除。意思是当队列满时，队列会阻塞插入元素的线程，指到线程不满；队列为空时，获取元素的线程会等待队列变为非空。
#### 6.3 Fork/Join框架
Fork/Join框架是Java7提供的一个用于并行执行任务的框架，是一个把大任务分割成若干个小任务，最终汇总每个小任务结果后得到大任务结果的框架。
##### 6.3.1 工作窃取算法
工作窃取算法是指某个线程从其他队列里窃取任务来执行。  
优点：充分利用线程进行并行计算，减少了线程的竞争。  
缺点：在某些情况下还是存在竞争，比如双端队列里只有一个任务时。且该算法会消耗更多的系统资源，比如创建多个线程和多个双端队列。
![image](https://img-blog.csdnimg.cn/11e900f4b30f4e41b4b2f661d256a2a4.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5ZGG5aSn546L,size_20,color_FFFFFF,t_70,g_se,x_16)
## 第七章 Java中的13个原子类操作类
JDK1.5开始提供Atomic包，提供了13个原子操作类。  
```
// AtomicInteger
// 通过死循环配合CAS进行原子更新操作
public final int getAndAddInt(Object var1, long var2, int var4) {
        int var5;
        do {
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

        return var5;
    }
```
## 第八章 Java中的并发工具类
#### 8.1 等待多线程完成的CountDownLatch
CountDownLatch允许一个或多个线程等待其他线程完成操作。
#### 8.2 同步屏障CyclicBarrier
CyclicBarrier，可循环使用的屏障。让一组线程到达一个屏障时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续运行。
> CountDownLatch的计数器只能用一次，而CyclicBarrier可以通过reset()方法重置。
#### 8.3 控制并发线程数的Semaphore
Semaphore是用来控制同时访问特定资源的线程数量，它通过协调各个线程，以保证合理的使用公共资源。可以用于做流量控制。
#### 8.4 线程间交换数据的Exchanger
Exchanger用于进行线程间的数据交换。
## 第九章 Java中的线程池
#### 9.1 线程池
线程池优点：
1. 降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。
2. 提高响应速度。当任务到达时，任务可以不需要等待线程创建就能立即执行。
3. 提高线程的可管理性。  
![image](https://img-blog.csdnimg.cn/2e4efbed288f431a8bb7278ae95021e7.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5ZGG5aSn546L,size_20,color_FFFFFF,t_70,g_se,x_16)
#### 9.2 合理配置线程池
- CPU密集型任务应配置尽可能小的线程，如配置CUP核数+1；
- IO密集型任务线程并不是一直在执行任务，则应配置尽可能多的线程，如2 * CPU核数；
- 执行时间不同的任务可以交给不同规模的线程池来处理，或者可以使用优先级队列，让执行时间短的任务先执行；
- 通过```Runtime.getRuntime().availableProcessors()```方法获得当前设备的CPU个数。
## 第十章 Executor框架






## 第十一章 Java并发编程实践


---
##### 1. CompletableFuture
```
CompletableFuture.runAsync() //无返回值
CompletableFuture.supplyAsync() //有返回值
thenApply()/thenApplyAsync() //异步回调
exceptionally() //任务执行发生异常时的回调方法
thenCombine()/thenAcceptBoth()/runAfterBoth() //将两个任务组合起来，只有两个都执行完才会执行某个任务
```
##### 2. CompletionService

```
//	创建线程池 
ExecutorService	executor = Executors.newFixedThreadPool(3); 
Future<Integer>	f1 = executor.submit(()->getPriceByS1()); 
Future<Integer>	f2=	executor.submit(()->getPriceByS2()); 
List list = Array.asList(f1, f2);
list.foreach(v -> v.get());  //此时f1线程等待时间很长，f2的结果则就会一直阻塞
```
```CompletionService```将线程池Executor和阻塞队列BlockingQueue的功能融合在了一起,在任务调用完成后，将submit返回的future放入到completionQueue。用户可以通过take()从队列中得到已经结束的任务。若队列为空，则会阻塞。
```
//	创建线程池 
ExecutorService	executor =	Executors.newFixedThreadPool(10); 
//	创建CompletionService 
CompletionService<Integer> cs =	new	ExecutorCompletionService<>(executor); 
cs.submit(()->getPriceByS1()); 
cs.submit(()->getPriceByS2()); 
cs.submit(()->getPriceByS3()); 
//	将询价结果异步保存到数据库 
for	(int i = 0; i < 3; i++)	{		
    Integer r =	cs.take().get(); //要take()从队列中取task
    executor.execute(()->save(r)); 
}
```
##### 3.

