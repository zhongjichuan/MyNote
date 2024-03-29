#### JVM快速回顾
JVM：https://www.jianshu.com/p/9e6841a895b4

---
## JVM自动内存管理
#### Java内存区域
![image](https://upload-images.jianshu.io/upload_images/4070621-2dfbe0fa4266a276.png?imageMogr2/auto-orient/strip|imageView2/2/w/964/format/webp)
- 程序计数器：通过改变计数器的值来选取下一条需要执行的字节码指令，如控制程序循环、跳转、异常处理等。程序计数器为线程私有，各线程之间线程之间计数器互不影响。
- Java虚拟机栈：常称为栈，也是线程私有的。线程中每调用一个方法，Java虚拟机都会同步创建一个==栈帧==用于存储**局部变量表**、操作数、动态连接（将符号引用转换成直接引用）、方法出口（记录方法在何处被调用）等信息。每个方法的调用和执行完毕，就对应着一个栈帧在虚拟机栈中的入栈和出栈。
> 局部变量表：存放编译期可知的各种基本数据类型，引用类型，局部变量表的大小在编译期便已经可以确定，在运行时期不会发生改变  
如果线程请求的栈深度大于虚拟机所允许的深度，会抛出StackOverflowError异常。如无限递归  
-Xss选项来设置线程的最大栈空间，栈的大小直接决定了函数调用的最大可达深度
- 本地方法栈：为虚拟机使用到的本地（Native）方法服务
- Java堆：被所有线程共享，用于存放对象实例
- 方法区：被线程共享，用于存储已被虚拟机加载的类型信息、常量、静态变量、即时编译后的代码Class等数据。在JDK8以前，使用**永久代**存放Class和元数据信息；现在使用**元空间**。元空间不在虚拟机设置的内存中，而是使用本地内存（系统内存）。
> 为什么放弃永久代，选择元空间？  
方法区是JVM的一个规范，永久代和元空间是其的实现。jdk7以前永久代是存在JVM内存中的，永久代内存经常不够用或发生内存泄漏；永久代也会为GC带来不必要的复杂度的，而且回收效率偏低。
- 运行时常量池：==是方法区的一部分==；Class文件中除了有类的版本、字段、方法等描述信息外，还有一项信息是常量池表（类中的常量），这部分信息会被加载到运行时常量池中，全局共享。虚拟机指令根据常量池找到要执行的类名、方法名、参数类型、字面量等类型。
#### HotSpot虚拟机对象
###### 对象的创建
1. 虚拟机检测到new指令时，首先确认new指令的参数能否在常量池中定位到一个类的符号引用，并检查这个符号引用代表的类是否已被加载、解析、初始化；如果没有，那会执行**类加载**过程。
2. 为新生对象分配内存。对象所需的内存在类加载完成后便可完全确定。  
> 分配内存的两种方式，选择哪种方式由所采用的垃圾收集器是否带有++空间压缩整理++的能力决定的：  
> ++指针碰撞++：堆中的内存是规整的，所有用过的内存度放一边，空闲的内存放另一边，中间放着一个指针作为分界点的指示器，所分配内存就仅仅是把指针向空闲空间那边挪动一段与对象大小相等的距离  
> ++空闲列表++：虚拟机维护一个列表，记录哪些内存是可用的
3. 内存分配完成后，虚拟机将分配到的内存空间（不包含对象头）都初始化为零值
4. 设置对象头
###### 对象的内存布局
- 对象头：
    - Mark Word(对象的哈希码，GC分代年龄，锁状态标志，线程持有的锁等)，32位字节的存储空间，每种状态存储不同的内容；  
![image](https://img-blog.csdnimg.cn/5d6b32a7b98843249d839ed2754585e5.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5Luy5bCP5bed,size_20,color_FFFFFF,t_70,g_se,x_16)
    - 类型指针，该指针指向它的类元数据（位于方法区，类元数据是描述类属性的信息），JVM通过这个指针确定对象是哪个类的实例
    - 是数组对象则还包含数组长度信息  

- 实例数据：类中定义的成员变量
- 对齐填充：用作占位符来填充内存。任何对象的大小都是8字节的整数倍。
###### 对象的访问定位
程序会通过栈上的reference数据来操作堆上的具体对象
- 句柄访问：在堆中开辟一块内存作为句柄池，reference中存储的就是对象的句柄地址，而句柄中包含了对象实例数据和类型数据的地址信息  
    > 好处：reference中存储的是稳定句柄地址，在对象被移动时只会改变句柄中的实例数据指针，而reference本身不需要修改
- 直接指针访问：reference中存储的直接就是对象地址，HotSpot主要的访问方式
    > 好处：速度更快，节省了一次指针定位的时间开销 

![image](https://img2020.cnblogs.com/blog/1117609/202003/1117609-20200327223929343-1244300162.jpg)![image](https://img2020.cnblogs.com/blog/1117609/202003/1117609-20200327223943696-1024093458.jpg)
#### 垃圾收集器和内存分配策略
###### 判断对象已死
- 引用计数法：在对象中添加一个引用计数器，当有地方引用这个对象的时候，引用计数器的值就加1；当引用失效的时候，计数器的值就减1；计数器为零则表示对象不可用。但是不能解决++对象之间循环引用++的问题。
- 可达性分析：以GC Roots的根对象作为起始节点集，根据引用关系向下搜索，搜索过程所走过的路径成为“引用链”，如果某个对象到GC Root间没有任何引用链相连，表明此对象不可用。
- GCRoot对象，当前时刻存活的对象：
1. 虚拟机栈（栈帧中的本地变量表）中的引用的对象；
2. 方法区中类静态属性引用的对象；
3. 方法区中常量引用的对象；
4. 本地方法栈中JNI（一般说的Native方法）的引用的对象。
###### 引用类型
- 强引用：普通代码，即普遍存在的引用赋值。只要引用关系还在，垃圾收集器就不会回收掉被引用的对象。
- 软引用：```SoftReference<Drawable> soft = new
 SoftReference<Drawable>(drawable);```用来描述有用但非必须的对象，在系统发生内存溢出异常前，才会对这些对象进行回收。
- 弱引用：```WeakReference<String> abcWeakRef = new WeakReference<String>(str);```弱引用所关联的对象只能生存到下一次垃圾收集为止
- 虚引用：作用是能在目标对象被收集器回收时收到一个系统通知
#### 垃圾收集算法
###### 标记-清除算法
标记所有需要回收的对象，在标记完成后，统一回收所有被标记的对象；或者发过来，标记存活对象，统一回收未被标记的对象。  
缺点：1. 需要进行大量的标记和清除动作；2. 会产生大量不连续的内存碎片
![image](https://img-blog.csdn.net/20180216211159152?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzU5NTQxOQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
###### 标记-复制算法
将内存按容量划分为大小相等的两块，每次只用其中一块。当这一块的内存用完，就将还存活着的对象复制到另一块内存中，然后把已使用过的内存一次清理掉。  
缺点：可用内存缩小为原来的一半
![image](https://img-blog.csdn.net/20180216211938306?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzU5NTQxOQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
**Appel式回收**  

将内存分为一块较大的Eden空间和两块较小的Survivor空间，每次只使用Eden和一块Survivor。发生垃圾收集时，将Eden和Survivor中仍存活的对象一次性复制到另一块Survivor空间上，然后直接清理Eden和已使用过的Survivor空间。Eden和两个Survivor的比例是8:1:1。  
当Survivor空间不够用时，需要依赖其他内存（这里指老年代）进行分配担保。因此当Survivor空间真的不足以存放上一次新生代收集下来的存活对象时，这些对象将直接通过分配担保机制进入老年代。
###### 标记-整理算法
标记过程与“标记-清除”算法一样，但后续是让所有存活的对象都向内存空间的另一端移动，然后直接清理掉边界以外的内存。
![image](https://img-blog.csdn.net/20180216214021290?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzU5NTQxOQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
###### 分代收集算法
分代收集算法是目前大部分JVM的垃圾收集器采用的算法。将堆区划分为老年代和新生代，老年代的特点是每次垃圾收集时只有少量对象需要被回收，而新生代的特点是每次垃圾回收时都有大量的对象需要被回收，那么就可以根据不同代的特点采取最适合的收集算法。
- 对于新生代会采取复制算法，因为在新生代中，java对象都具有==朝生夕==死的特性，每次垃圾收集时都会发现大量对象死去，只有少量存活，也就是说仅需要少量的复制操作。==新生代GC==（Minor GC / YGC）。
- 对于老年代会采用标记-清除算法或标记-整理算法，因为对象存活率高，也没有额外空间对它进行分配担保。==老年代GC==（Major GC / Old GC）。
- 整堆收集（==Full GC==）：收集整个Java堆和方法区的垃圾收集。Full GC和Major GC在不同资料上定义混淆。
    - 触发Full GC时机：
    - 1.调用System.gc()，这只是建议虚拟机执行 Full GC，但是虚拟机不一定真正去执行
    - 2.老年代空间不足
    - 3.空间分配担保失败，老年代剩余空间小于每次晋升的对象的平均大小
    - 4.JDK 1.7 及以前的（永久代）空间满
- 混合收集（Mixed GC）:收集整个新生代以及部分老年代的垃圾收集。目前只有G1 GC支持，G1 GC将内存空间划分region进行管理。
> Stop-the-World，简称STW，指的是GC事件发生过程中，会产生应用程序的停顿。停顿产生时整个应用程序线程都会被暂停，没有任何响应，有点像卡死的感觉，这个停顿称为STW
#### 垃圾收集器
###### Serial收集器
Serial收集器是最基础，历史最悠久的收集器。它是个单线程工作的收集器，它只会使用一个处理器或一条收集线程去完成垃圾收集工作，但是在它进行垃圾收集时，必须暂停其他所有的工作线程，直至收集结束。用户体验及其不友好，适合用在桌面应用。  
![image](https://pic.yupoo.com/crowhawk/6b90388c/6c281cf0.png)
###### ParNew收集器
实质上是Serial收集器的多线程并行版本，会同时使用多条线程进行垃圾收集。老年代使用的还是Serial Old搜集器。  
![image](https://pic.yupoo.com/crowhawk/605f57b5/75122b84.png)
###### Parallel Scavenge收集器
Parallel Scavenge收集器也是一个支持并行收集**的多线程**新生代收集器，也是基于标记-复制算法实现的收集器。但它的关注点与其他收集器不同，CMS等收集器的关注点是尽可能缩短垃圾收集时用户线程的停顿时间，而Parallel Scavenge收集器的目标是达到一个**可控制的吞吐量**  
吞吐量=运行用户代码时间/（运行用户代码时间+运行垃圾收集时间  
**GC的自适应调节策略**：虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量。设置参数：```-XX:+UseAdaptiveSizePolicy```
###### Serial Old收集器
Serial Old是Serial收集器的老年代版本，是一个单线程收集器，使用标记-整理算法。 
- 供客户端模式下的HotSpot虚拟机使用
- 服务器模式下，1在JDK5之前与Parallel Scavenge收集器搭配使用；2作为CMS收集器发生失败时的后备预案。
![image](https://pic.yupoo.com/crowhawk/6b90388c/6c281cf0.png)

###### Parallel Old收集器
Parallel Old收集器是Parallel Scavenge收集器的老年代版本，使用多线程和“标记-整理”算法。  
Parallel Scavenge之前都是和Serial Old收集器搭配使用，现在可以和Parallel Old收集器搭配使用，可以**处理注重吞吐量或者CPU资源较为紧张**的场合。
![image](https://pic.yupoo.com/crowhawk/9a6b1249/b1800d45.png)
###### CMS收集器
CMS收集器是一种以获**取最短回收停顿时间**为目标的收集器，基于“标记-清除”算法，整个过程分为四个步骤：
1. 初始标记：只是标记一下GC Roots能直接关联到的对象，速度很快，++需要停顿用户线程++（不然根节点的对象引用关系会不断的变化）
2. 并发标记：从GC Roots的直接关联对象开始遍历整个对象图的过程，耗时较长
3. 重新标记：修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，++需要停顿用户线程++，进行标记
4. 并发清除：清理删除掉已经死亡的对象  
 
![image](https://pic.yupoo.com/crowhawk/fffcf9a2/f60599b2.png)  
CMS收集器缺点：  
1. 对CPU资源非常敏感，回收多线程会争夺CPU资源，导致用户程序执行速度变慢
2. 无法处理“浮动垃圾"，用户程序和回收线程同时运行，会不停的产生垃圾对象，CMS无法在当次收集中处理它们，只能留待下一次再清理。也是由于用户线程一直在运行，那就需要预留内存空间提供给用户线程使用，因此CMS收集器不能像其他收集器等待老年代几乎被填满了再进行收集。如果预留的内存空间不足，则会出现“并发失败”，虚拟机会启动后备预案：临时启用Serial Old收集器，但这样停顿时间就很长了
3. 会产生大量的空间碎片，CMS收集器是基于“标记-清除”算法实现的，意味着在收集结束时会有大量的空间碎片产生
###### G1收集器
G1收集器开创了面向局部收集的设计思路和基于Region的内存布局形式。G1不在坚持固定大小以及固定数量的分代区域划分，而是把连续的Java堆划分为多个大小相等的独立区域（Region），每个Region可以根据需要扮演新生代的Eden空间，Survivor空间或者老年代空间。还有一类特殊的Humongous区域，专门用来存储大对象。G1允许用户设定最大停顿时间（默认200ms），但是如果设置的时间过短，每次能回收的垃圾就只占堆内存很小的一部分，收集器收集的速度跟不上垃圾产生的速度，最终垃圾沾满堆引发Full GC反而降低性能。  
![G1的内存划分](https://img-blog.csdnimg.cn/20200730161553866.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMjI4MTI4NDk=,size_16,color_FFFFFF,t_70)  
G1收集器运行步骤：
1. 初始标记：标记CG Roots能直接关联到的对象，并修改TAMS(Top at Mark Start)指针的值，让下一阶段用户线程能正确地在可用的Region中分配新对象。这个阶段**需要停顿线程**。
2. 并发标记：从GC Roots开始对堆中的对象进行可达性分析，找出要回收的对象，但可与用户程序并发执行。
3. 最终标记：暂停用户线程，处理并发阶段结束后遗留下来的的最后那少量的SATB记录（处理用户线程运行时新的对象引用关系）。
4. 筛选回收：首先对各个Region中的回收价值和成本进行排序，根据用户所期望的GC 停顿是时间来制定回收计划。然后把决定回收的一部分Region的存活对象复制到空的Region中，再清理整个旧的Region的全部空间。这里涉及存活对象的移动，需要暂停用户线程。  
![image](https://pic.yupoo.com/crowhawk/53b7a589/0bce1667.png)
#### 内存分配原则
###### 对象优先在Eden分配
JVM优先把对象放入Eden区，若Eden空间不足以分配新对象，则会触发Minor GC，存活的对象将会被赋值到Survivor中；若Survivor空间不足，JVM会通过**分配担保机制**将存活对象提前转移到老年代去。
###### 大对象直接进入老年代
大对象指需要大量连续内存空间的Java对象，如很长的字符串，元素数量庞大的数组。JVM避免大对象的原因是，在分配空间时，大对象容易导致内存还有不少空间就提前触发了垃圾回收，而且赋值对象也要高额的内存。JVM提供了```-XX:PretenureSizeThreshold```参数，指定大于该设置值的对象直接在老年代分配。
###### 长期存活的对象进入老年代
JVM给每个对象头中定义了一个年龄计数器，对象在Survivor区中每熬过一次Minor GC，年龄就+1，当年龄增加到一定的程度（**默认为15**），就会晋升到老年代。年龄阈值可以通过参数```-XX:MaxTenuringThreshold```设置。
###### 动态对象年龄判定
JVM并不是永远要求年龄必须达到阈值才能晋升老年代。如果Survivor空间中低于或等于某年龄的所有对象大小的总和大于Survivor空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代。例```年龄1 + 年龄2 > survivor空间/2```，则回收年龄2，年龄3...
###### 空间分配担保
发生MinorGC前，JVM需要检查老年代最大可用的连续空间是否大于新生代所有对象空间，如果成立，那这一次Minor GC可以确保是安全的。不成立，则继续检查老年代最大可用连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，进行Minor GC(如果 Survivor无法容纳存活的对象，这些对象会被送入老年代)；小于则进行Full GC。
> 什么对象才能进入老年代？
>1. 大对象直接进入老年代
>2. survivor无法放下Minor GC后存活的对象，这些对象直接进入老年代
>3. 对象的年龄大于阈值15次，进入老年代
>4. 动态年龄判断，例```年龄1 + 年龄2 > survivor空间/2```，则回收年龄2，年龄3...
#### TLAB
在内存分配对象时，存在多个线程对某块内存并发分配的情况。TLAB则是JVM为每个线程分配一个私有缓存区域，在Eden区，仅占1%。如果分配的是大对象，则直接采用CAS加锁重试的方式分配内存。

#### 堆空间的参数设置
```java
-XX:+PrintFlagsInitial  //查看所有的参数的默认初始值
-XX:+PrintFlagsFinal  //查看所有的参数的最终值（可能会存在修改，不再是初始值）
-Xms  //初始堆空间内存（默认为物理内存的1/64）
-Xmx  //最大堆空间内存（默认为物理内存的1/4）
-Xmn  //设置新生代的大小。（初始值及最大值）
-XX:NewRatio  //配置新生代与老年代在堆结构的占比
-XX:SurvivorRatio  //设置新生代中Eden和S0/S1空间的比例
-XX:MaxTenuringThreshold  //设置新生代垃圾的最大年龄
-XX:+PrintGCDetails //输出详细的GC处理日志
//打印gc简要信息：①-Xx：+PrintGC ② - verbose:gc
-XX:HandlePromotionFalilure：//是否设置空间分配担保
```
#### 虚拟机工具
###### jps 虚拟机进程状况工具
jps功能和Linux中的ps命令类似，可以列出正在运行的虚拟机进程，并显示虚拟机执行主类名称以及进程ID。  
![image](https://img-blog.csdnimg.cn/093570798d274b289b1a1931bfe0bbbc.png)
###### jstat 虚拟机统计信息监视工具
jstat(JVM Statistics Monitoring Tool)是用于监视虚拟机各种运行状态信息的命令行工具。
![image](https://img-blog.csdnimg.cn/0b004919522f4dfdb57eada86076da55.png)
###### jinfo Java配置信息工具
jinfo可以实时查看和调整虚拟机各项参数。
###### jmap Java内存映像工具
jmap命令用于生成堆转储快照。
###### jhat 堆转储快照分析工具
配合jmap使用，分析堆转储快照。
###### jstack Java堆栈跟踪工具
线程出现停顿时通过jstack来查看**各个线程**的调用堆栈，就可以获知没有响应的线程在后台在做什么，或者等待着什么资源。
###### VisualVM
命令行输入```jvisualvm```启动VisualVM。通过插件Visual GC可查看堆内存情况。
###### Arthas 阿尔萨斯
https://arthas.aliyun.com/doc/


#### 调优案列分析
Jvm调优的目标之一便是减少程序Full GC的次数，避免STW带来的影响。


---
## 虚拟机执行子系统
#### 类文件结构
###### Class类文件结构
Class文件是一组以字节为基础单位的二进制流，由无符号数和表组成。  
![image](https://pic1.zhimg.com/80/v2-d627f33b6e850bc2efe0fac63c241280_720w.jpg)
- 魔数：作用是为了让JVM能够识别这是一个字节码文件，魔数值为“0xcafebabe”。图片等文件也有魔数。
- 常量池：常量池计数器记录常量池容量计数值；常量池里存放字面量和符号引用。
    - 符号引用包含
    - 类和接口的全限定名，字段的名称和描述符
    - 方法的名称和描述符，方法句柄和方法类型
    - 等等...
- 访问标志：标明这个Class是类还是接口，是否是public类型，是否被声明final、abstract等。
- 类索引，父类索引，接口索引集合：确定该类的继承关系，确定类和父类的权限名。
- 字段表集合：用于描述接口或者类中声明的变量，包括public，static，volatile等修饰符，字段名称等。
- 方法表集合：用于描述类中的方法，同字段表一样。方法里的Java代码经过Javac编译器编译成字节码指令后，存放在方法属性表集合中一个名为“Code”的属性里面。
- 属性表集合：Class文件、字段表、方法表都可以携带自己的属性表集合，以描述某些场景专有的信息。方法的代码块放在Code中，final常量放在ConstantValue中。
###### 字节码指令
Java虚拟机的指令由一个字节长度的、代表着某种特定操作含义的数字（称为操作码，Opcode）以及跟随其后的零至多个代表此操作所需参数（称为操作数，Operands）而构成。
#### 虚拟机类加载机制
![image](https://img-blog.csdnimg.cn/20201121203257325.png?x-oss-process=image)
#### 类加载过程
- 加载阶段，通过类的全限定名，将class文件中的二进制数据读取到内存之中，然后将该字节流所代表的静态存储结构转换为方法区中运行时的数据结构，并在堆内存中生成一个代表该类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口。
- 连接阶段
    - 验证，确保被加载类的正确性，字节流是否符合JVM规范
    - 准备，为该对象的**静态变量**分配内存，并设置默认值，比如int默认值为0。final修饰的变量在准备阶段会被直接赋予正确的值。
    - 解析，**将常量池中的符号引用替换为直接引用**，针对该对象对象中引用的类接口、字段、类方法和接口方法进行解析。在编译的时候一个每个java类都会被编译成一个class文件，但在编译的时候虚拟机并不知道所引用类的地址，所以就用符号引用来代替，而在解析阶段就是为了把这个符号引用转化成真正的地址的阶段。
- 初始化阶段，为类中的变量赋予正确的值，会执行类构造器方法<clinit>()方法（这是Javac编译器的自动生成物)，为所有类变量的赋值动作和静态语句块。先父后子，先静态，再代码块，后构造。  
PS：静态代码块会在类第一次被加载到内存中时执行，<clinit>()方法只会被执行一次，所以++静态代码块只会被执行一次++。无论被new()多少次。  
> 类进行初始化时机：
> 1. new一个实例，读取一个类的静态字段，调用静态方法
> 2. 对类型进行反射调用的时候
> 3. 当初始化类时，需要优先触发其父类的初始化
> 4. 虚拟机启动时会先初始化main()方法所在的类
> 5. JDK1.8中接口有default修饰的方法，如果接口的实现类发生了初始化，那该接口要在其之前被初始化
#### 类加载器
![image](https://img-blog.csdnimg.cn/20201121203910562.png?x-oss-process=image)
1. 启动类加载器：负责加载<JAVA_HOME>\lib目录，而且是JVM能够识别的类库。启动类加载器无法被Java程序直接引用
2. 扩展类加载器：负责加载<JAVA_HOME>\lib\ext目录，是Java系统类库的扩展类库。可以在程序中直接使用扩展类加载器来加载Class文件
3. 应用程序类加载器：负责加载用户类路径上所有的类库，可以在程序中直接使用这个类加载器
#### 双亲委派
###### 工作过程
如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成，因此所有的加载请求最终都应该传送到顶层的启动类加载器中，只有当父加载器反馈自己无法完成加载请求（它的搜索范围中没有找到所需的类）时，子加载器才会尝试自己去加载。  
**沙箱机制**将java代码限定在虚拟机(JVM)特定的运行范围中，并且严格限制代码对本地系统资源访问，通过这样的措施来保证对代码的有效隔离，防止对本地系统造成破坏，保证java程序运行的稳定。
> 例如类java.lang.Object存放在rt.jar中，无论哪一个类加载器要加载这个类，最终都是最顶端的启动类加载器进行加载，因此Object类在程序中始终能保证是同一个类。
###### 破坏双亲委派模型
核心：要继承ClassLoader类，重写loadClass()方法，不去使用父类加载器进行加载
> tomcat为什么要打破双亲委派机制？
> 一个web容器可能需要部署多个应用程序，每个程序的类库的版本可能不同，但类加载只根据类的全限定名，使用默认的类加载器，会导致tomcat中无法存在不同版本的类库。

> JDBC为什么打破双亲委派？  
> Java核心库提供了很多接口，但是接口的实现类则来自不同的厂家提供的Jar包，启动类加载器只能加载核心库，而接口的实现类就需要子类加载器进行加载。可以通过线程上下文件类加载器(Thread Context ClassLoader)获取应用程序类加载器，去加载接口实现类。这也打破了委托机制（A类调用B类,B类则由A类加载器加载），这里的DriverManager类和驱动实现类是由不同的加载器进行加载的。
> DriverManager类中有静态代码块，加载DriverManager类时会主动去加载驱动Driver类。最终通过Class.forName()创建Driver对象，同时也会执行Driver中的静态代码块，即将加载到的Driver注册给DriverManager。  
#### 运行时栈帧结构
JVM以**方法**作为最基本的执行单元，**栈帧**是支持方法调用和执行的数据结构，也是虚拟机栈的栈元素。栈帧存储了++方法的局部变量表、操作数栈、动态连接和方法返回地址++等信息。每一个方法从调用开始至执行完成的过程，都对应着一个栈帧在虚拟机栈里面从入栈到出栈的过程。  
![image](https://imgconvert.csdnimg.cn/aHR0cDovL3BpY3R1cmUudGp0dWxvbmcudG9wLyVFNiVBMCU4OCVFNSVCOCVBNy5KUEc)  
###### 局部表量表
局部表量表是一组标量值存储空间，用于存放方法参数和方法内部定义的局部变量。在编译成Class文件时，就在方法的Code属性中确定了方法所需分配的局部变量表的最大容量。  
局部变量表的容量以变量槽为最小单位，每个变量槽可以占用32位以内的内存，能存放一个boolean、byte、char、short、int、float、reference和returnAddress类型的数据。  
为了节省栈帧耗用的内存空间，局部变量表中的变量槽是可以重用的。方法体中定义的变量不一定会覆盖整个方法体，若果当前字节码PC计数器的值已经超出某个变量的作用域，那这个变量对应的变量槽就可以交给其它变量来重用。
###### 操作数栈
操作数栈是一个后入先出栈。当一个方法执行开始时，这个方法的操作数栈是空的，在方法执行过程中，会有各种字节码指令往操作数栈中写入和提取内容，也就是出栈/入栈操作。  
###### 动态连接
每个栈帧都包含一个指向运行时常量池中该栈帧所属方法的引用，持有这个引用是为了支持方法调用过程中的动态连接（就是将符号引用转换成直接引用的过程，找到方法所属对象的内存地址）。Class文件的常量池中存有大量的符号引用，字节码中的方法调用指令就以常量池里指向方法的符号引用作为参数。这些符号引用一部分会在**类加载阶段或者第一次使用的时候**就被转化为直接引用，这种转化被称为**静态解析**。另外一部分将在每一次**运行期间**都转化为直接引用，这部分就称为**动态连接**。
###### 方法返回地址（方法出口）
当一个方法执行时，只有两种方式退出这个方法。一是执行引擎遇到方法返回的字节码指令；二是方法执行过程中遇到异常。方法退出后，需要返回最初方法被调用时的位置。方法正常退出时，主调方法的PC计数器的值就可以作为返回值。
#### 方法调用
###### 解析
所有方法的调用的目标方法在Class文件里都是一个常量池的符号引用，在类加载的解析阶段，会将其中的一部分符号引用转化为直接引用（一个指针或偏移量，可以让JVM快速定位到具体要调用的方法）。这种情况成立的前提是，方法++在编译期就已经确定下来，不会在运行期改变++，方法只有一个确定的版本，主要有静态方法和私有方法（不会被继承）。
###### 分派
静态类型和实际类型在程序中都可以发生一些变化，区别是静态类型的变化仅仅在使用时发生，变量本身的静态类型不会被改变，并且最终的静态类型是在编译期可知的；而实际类型变化的结果在运行期才可确定，编译器在编译期并不知道一个对象的实际类型是什么。
- 静态分派：
虚拟机在重载时是通过**参数的****静态类型**而不是实际类型作为判定依据的。静态分派的最典型应用表现就是方法**重载**。
- 动态分派：在运行期间根据**实际类型**确定方法版本的分派过程为动态分派。体现为**重写**。关键是invokevirtual指令的解析过程，由于invokevirtual指令执行的第一步就是在运行期方法所属对象的**实际类型**，如果在实际类型中找到与常量中的描述符和简单名称都相符的方法，则返回这个方法的直接引用；否则继续在实际类型的父类中进行搜索。这个过程就是方法重写的本质。
#### 解释器
解释器真正意义上所承担的角色就是一个运行时“翻译者”，将字节码文件中的内容“翻译”为对应平台的本地机器指令执行。

---
## 程序编译与代码优化
#### Java编译器
- 前端编译器：把.java变成.class的。如Sun的Javac,Eclipse JDT中的增量式编译器。
- 即时编译器：JIT,把字节码转变成机器码
- 提前编译器：直接将*.java文件编译本地机器码
#### 编译过程
![image](https://img2018.cnblogs.com/blog/639237/201906/639237-20190629234329252-1925470570.png)
#### Java代码执行过程
![image](https://www.yuque.com/api/filetransfer/images?url=https%3A%2F%2Fgitee.com%2Fvectorx%2FImageCloud%2Fraw%2Fmaster%2Fimg%2F20210507124722.png&sign=8f4e06b2070a3b3396eea3e796e011a815a034de378a9370f9564b9f850b527d)
##### 1. 解析与填充符号表
###### 1.1 词法、语法解析
词法解析：将源代码的字符流转变成标记（Token）集合，标记是编译过程的最小元素，如“int a = b+2"包含了6个标记，分别是int，a，=，b，+，2。  
语法分析：根据标记序列构造语法抽象树的过程,抽象语法树（AST）是一种用来描述程序语法结构的树形表示方式，语法树的每一个节点都代表一个语法结构，例如包，类型，修饰符，接口，返回值甚至代码注释。  
经过词法和语法分析生成语法树后，编译器就不会再对源码字符流进行操作了，后续的操作都建立在抽象语法树之上。
###### 1.2 填充符号表
完成抽象语法树之后，下一步就是填充符号表的过程。符号表是由一组符号地址和符号信息构成的表格，类似于哈希表中K-V值对的形式。符号表所登记的信息在编译的不同阶段都要用到，譬如用于语义检查和产生中间代码。
##### 2. 注解处理器
注解处理器在运行期发挥作用，++这些注解会对语法树进行修改++。譬如Lombok插件相关的注解。
##### 3. 语义分析与字节码生成
语义分析：编译器获得查程序代码的抽象语法树表示，语法树能表示一个结构正确的源程序，但无法保证源程序语义正确。语义分析则是对程序进行检查，如类型检查、控制流检查等。
1. 标注检查：检查变量使用前是否已被声明、变量与赋值之间的数据类型是否能够匹配等。
2. 数据及控制流分析：检查是否所有的受查异常都被正确处理；方法的每条路径是否都有返回值。
3. 解语法糖：++**语法糖**指在计算机语言中添加某种语法，更方便程序员使用该语言。例如泛型、变长参数、自动装箱拆箱等等++。虚拟机运行时并不直接支持这些语法，它们在编译阶段被还原回原始的基础语法结构，这个过程为解语法糖。
4. 字节码生成：不仅仅是把前面各个步骤所生成的信息（语法树、符号表）转化为字节码写入磁盘中，编译器还进行了少量代码添加和转换工作。
#### 语法糖的味道
###### 泛型
泛型的本质是参数化类型或者参数化多态的应用。可以用在类、接口和方法的创建中。Java语言中的泛型只存在程序源码中，在编译后的字节码文件中，全部泛型都被替换成原来的裸类型(**类型擦除**)，并且在相应的地方插入了强制转型代码。  
例：```List<Integer>```和```List<String>```在编译之后都会被擦除，变成了同一种的裸类型List。
###### 自动装箱、拆箱与遍历循环（for-each循环）
> - ==：如果是基本数据类型，则直接对值进行比较，如果是引用数据类型，则是对他们的地址进行比较（但是只能比较相同类型的对象，或者比较父类对象和子类对象）  
> - equals方法继承自Object类，也是对对象的地址进行比较，而JDK类中有一些类覆盖了Object类的equals()方法，比较规则为：如果两个对象的类型一致，并且内容一致，则返回true。这些类有包装类（Integer,Double等），String，Date等
###### 条件编译(if,while)
判断条件为常量时，如```if(true){}else{}```，编译器会把分支中不成立的代码块消除掉。
#### 编译器优化技术
##### 方法内联
##### 逃逸分析
逃逸分析是目前JVM中比较前沿的优化技术，并不是直接优化代码的手段，而是为其他优化措施提供依据的分析技术。  
逃逸分析原理： 分析对象动态作用域，当一个对象在方法里被定义后，他可能被外部方法所引用，例如作为调用参数传递到其他方法中，这种称为**方法逃逸**；还有可能被外部线程访问到，称为**线程逃逸**。  
如果能证明一个对象不会逃逸到方法或线程之外，则可能为这个对象实例采取不同程度的优化，如：
- **栈上分配**：通常对象的内存空间是在堆中分配的，对应的GC回收需要进行标记筛选，回收和整理内存，都需要耗费大量的资源。++将完全不会逃逸的局部对象和不会逃逸出线程的对象在栈上分配内存，对象所占用的内存空间就可以随栈帧出栈而销毁，这样GC的压力将会下降很多。++
- **标量替换**：假如逃逸分析能够证明一个对象不会被方法外部访问，并且这个对象可以被拆散，那么程序真正执行的时候将可能不会去创建这个对象，而改为直接创建它的若干个被这个方法使用的成员变量来代替。

```
public int test(int x){
    int xx = x + 2;
    Point p = new Point();  //对象p不会发生逃逸
    return p.getX();
}
// 优化后
public int test(int x){
    return x + 2;
}
```
- **同步消除（锁的消除）**：线程同步（synchronized）是一个相对耗时的过程，如果逃逸分析能够确定一个变量不会逃逸出线程，无法被其他线程访问，那这个变量的读写就不会有竞争，那么对这个变量实施的同步措施就可以消除。
---
## 高效并发
### Java内存模型
见[多线程.md](http://note.youdao.com/noteshare?id=66fd133dcc6daf660f265bd4b81bb4e3&sub=464F8A920A8D479BA8CE160424684E7A)
#### 线程安全
定义：当多个线程同时访问一个对象时，如果不用考虑这些线程在运行时环境下的调度和交替执行，也不需要进行额外的同步，或者在调用方进行任何其他的协调操作，操作这个对象的行为都可以获得正确的结果，就称为这个对象时线程安全的。
线程安全实现的同步方法：
1. 互斥同步：线程互斥，使用synchronized，JUC
2. 非阻塞同步：CAS机制
3. 无同步方案：使用ThreadLocal实现线程本地存储
