## 一、 MySql架构
![image](https://img-blog.csdnimg.cn/524b0bc3aa704651b11068c1d378949e.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzQwNTczMw==,size_16,color_FFFFFF,t_70#pic_center)
#### 1. server层
Server层包括连接器、查询缓存、分析器、优化器、执行器等，涵盖MySQL的大多数核心服务功能，以及所有的内置函数（如日期、时间、数学和加密函数等），所有跨存储引擎的功能都在这一层实现，比如存储过程、触发器、视图等。  
- 连接器：负责客户端跟服务器建立连接、身份认证（用户名、客户端IP、密码是否正确 / 证书是否正确等）、权限验证、维持和管理连接等操作
- 查询缓存：缓存开关开启后，执行过的语句和结果都会缓存到内存中。每次执行查询语句时，都会先检查是否命中缓存
- 分析器：进行词法解析和语法解析，判断sql是否正确，例如验证是否使用错误的关键字，或使用关键字的顺序是否正确，最终生成一个解析树
- 优化器：mysql使用基于成本的优化器，根据解析树生成不同的执行计划，会尝试预测一个查询使用某种执行计划时的成本，并选择成本最小的一个
    > 主要优化点
    > - 调整联合索引的位置，以便遵循最左前缀原则
    > - 当有多个索引可用的时候，选择使用哪个索引
    > - 在一个语句有多表关联（join）的时候，决定各个表的连接顺序，以哪个表为基准表
    > - 覆盖索引扫描
    > - 提前终止查询，在发现已经满足查询需求是，mysql能立即终止查询。例如：limit语句
- **执行器**：通过分析器知道要做什么，通过优化器知道知道该怎么做，得到一个执行计划，调用存储引擎的API来执行查询
#### 2. 存储引擎层
存储引擎层负责数据的存储和提取
#### 3. InnoDB和MyISAM的区别
| | InnoDB | MyISAM | 
|:--:|---|---
|事务 | InnoDB支持事务 | MyISAM不支持事务
|外键 | InnoDB支持外键 | MyISAM不支持外键
|锁 | 有表锁和行锁，默认锁的粒度为行级锁，并发度更高，但是更容易发生死锁 | 只有表锁，默认锁的粒度为表锁，并发度较低，但是发生死锁的概率较低
|索引 | 聚簇索引，索引和数据放在一起，B+树叶子节点放置主键和实际数据 | 非聚簇索引，索引文件和数据文件分开放置，B+树的叶子结点放置数据地址指针
|文件 | 分为.frm（表空间）和.idb（数据和索引）两个文件 | 分为.frm（表空间）、.myi（索引）和.myd（数据）三个文件
|缓存 | 查询会同时缓存索引和数据 | 查询只有缓存索引
#### 4. MySql存储结构
内存与磁盘的交互数据是以页为单位的，页的大小一般默认是4K或8K，innodb存储引擎默认为16K。  
通过索引减少数据库与磁盘的IO次数，可以加快查询速度。
## 二、索引
#### 1. InnoDB索引
索引是存储引擎用于快速查找到记录的一种数据结构。先在索引中找到对应值，然后根据匹配的索引记录找到对应的数据行。
###### InnoDB索引图
![image](https://img-blog.csdnimg.cn/fe02c290beb24df7b446fc8a15817675.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzQwNTczMw==,size_16,color_FFFFFF,t_70#pic_center)

#### B+树索引
B+树由二叉树，平衡二叉树，B-tree演变而来。
###### 二叉树
二叉树的查找时间复杂度可以达到O(log2(n))，左子树节点小于右子树节点。如果有数据为1,2,3，4，那么该树为以1为根节点，树的高度为4，类似一个链表，即查询需要全表扫描。
###### 平衡二叉树
节点的子节点高度差不能超过1，这样就解决了二叉树线性链表的问题。但为保证二叉树的平衡，需要通过左旋右旋等操作；而且树的高度太高，会进行多次IO操作。
###### B-tree、多路平衡查找树
![image](https://img-blog.csdnimg.cn/d59b5795491242d7b3c7e69739b6fbd5.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5Luy5bCP5bed,size_20,color_FFFFFF,t_70,g_se,x_16)


> B-tree非叶子节点不仅存储索引值，也存储数据。如果数据data较大，则每个节点可存储的索引数量就会很少，导致B-tree的高度变大。每次索引页的读取都是一次磁盘IO操作，树的高度大了，IO次数可能就会多，进而影响查询效率。B+tree的索引页能存放更多的索引，树的高度低。  
B+Tree扫库和扫表能力更强。如果我们要根据索引去进行数据表的扫描，对B TREE进行扫描，需要把整棵树遍历一遍，而B+TREE只需要遍历他的所有叶子节点即可（叶子节点之间有引用）  

[Mysql索引选择B+tree的原因](https://blog.csdn.net/b_x_p/article/details/86434387)
#### 聚簇索引
聚簇索引并不是一种单独的索引类型，而是一种数据存储方式。聚簇索引的叶子节点保存了一个完整的记录行。  
InnoDB通过主键聚集数据，如果没有定义主键，InnoDB会选择一个唯一的非空索引代替。若没有这样的所有，InnoDB则会隐式定义一个主键作为聚簇索引。  
> 尽量使用自增长主键的原因？  
索引是有序的，Mysql的数据存放顺序与聚簇索引的顺序一致，如果每页的记录满了，则会开辟一个新的页。使用自增主键的话，那么每次插入新的记录，记录就会顺序添加到当前索引节点的后续位置。如果主键不是自增长，插入新数据，很容易造成**页分裂和页移动**，造成很大的维护成本。
#### 二级索引（辅助索引）
非聚簇索引则为二级索引。
> - 二级索引访问需要两次索引查找，二级索引的叶子节点保存的是行的++主键值++，再通过主键值去聚簇索引中查找对应的行（**回表**）。  
> - **索引下推**：ICP优化，是mysql5.6后优化器增加的一步操作，指尽量利用索引信息，++减少回表的次数++。通过索引定位到主键，再根据where中的其他条件配合联合索引，过滤出符合条件的主键，再去回表。
> - 主键的大小会影响所有索引的大小。
#### 联合索引（复合索引）
两个或更多个列上的索引被称作联合索引。
- [ ] 最左前缀法则：查询从索引的最左前列开始并且不跳过索引中的列
#### 覆盖索引
如果一个索引包含所需要查询的字段，就是覆盖索引。InnoDB的二级索引在叶子节点中保存了行的主键值，所以如果二级主键能够覆盖查询，则可以避免对主键索引的二次查询（回表）。  
> - 表中有联合索引（a, b），select a, b则为覆盖索引查询。首先通过联合索引最左匹配原则，由索引a定位到二级索引，此时二级索引中已有b的数据，已不需要再进行回表操作。  
> - EXPLAIN的Extra列为"Using index"，则表示字段的信息直接从索引树中的信息取得，并没有回表。
#### 冗余、重复索引
- 重复索引：在相同的列上按照相同的顺序创建的相同类型的索引。
     > 例如主键设置唯一限制并加上索引；  
     如果列的索引类型不同，则并不是重复索引。
- 冗余索引：指两个索引所达到的效果有重叠
    > 例如创建索引（A,B），再创建索引（A）就是冗余索引；  
    或者创建索引（A,ID），ID是主键，这也是冗余索引，因为二级索引中已包含主键。
#### 全文索引
通过数值比较、范围过滤等就可以完成绝大多数的查询。但是如果需要通过关键字的匹配来进行查询过滤，就需要基于相似度的查询，而不是精确的数值比较。全文索引就是为这种场景设计的。互联网搜索引擎使用的就是全文索引技术。
#### 索引排序
order by 使用where条件中的索引字段排序，where age = 10 order by age，避免 using filessort 排序。
> - 使用索引扫描排序，索引本身就是有序的，所以不需要再次进行排序
> - using filessort：在内存中排序，占用CPU资源。如果查询结果太大还会产生临时文件，到磁盘中进行排序，这时候会进行大量IO操作性能较差；
#### 索引失效情况
- 不满足**最左前缀原则**，需要考虑联合索引的匹配规则，如（a,b），**前者相等的情况后者才有序**，索引无序的情况下是无法使用的
     > - where条件即便为b = 1 and a = 2, 但优化器会自动优化，调整顺序，使索引生效。
     > - where条件为a > 1 and b = 2，索引不生效
- like查询左边有%，例 like '%a'。字符串索引也是按字母排序，需要匹配最左前缀原则
- 使用or，a = 1 or b = 2，若是联合索引（a,b）则不生效；a，b若都是单列索引，mysql通过**Index Merge**，对多个索引分别进行条件扫描，然后将它们各自的结果进行合并union，从而使索引生效
- 隐式类型转换，where mobile = '133123' 与 where mobile = 133123
- 索引列上有计算，使用了函数，where a+1 = 2；
- 字段不允许为空，is null 或 is not null 不走索引；字段允许为空，is null 和is not null都会走索引
- 连表查询的两张表字段字符集不同
- in会走索引，但是当IN的取值范围较大时会导致索引失效，走全表扫描。==数据太多或太少，索引都会失效。优化器会判断判断走索引的成本和全表扫描的成本，数据太少，或许不走索引；索引区分度过低，需要过多的回表，也可能不走索引==
- 使用负向查询（not ，not in， not like ，<> ,!= ,!> ,!< ） 不会使用索引
- 不可一概而论，**不同版本**的数据库，结果也会有差异；
#### 索引建立原则
1. 为经常需要查询、排序、分组和联表操作的字段建立索引；
2. 表的数据量较少，不建议使用索引，查询花费的时间可能比遍历索引的时间还要短；
3. 索引不是越多越好，每个索引都需要占用磁盘空间，索引越多，需要的磁盘空间就越大。在修改表的内容时，索引必须进行更新，有时还可能需要重构。因此，索引越多，更新表的时间就越长。
4. 频繁更新的列不适合作为索引，会增加索引的维护成本；
5. 区分度不高的列不适合作索引，例如性别，只有男女，通过索引查出来的数据量依旧很大，还有做大量的回表操作；
6. 如果索引字段的值很长，最好使用值的前缀来索引。例如，TEXT和BLOG类型的字段，进行全文检索会很浪费时间。如果只检索字段的前面的若干个字符，这样可以提高检索速度。邮箱索引有个很巧妙的方法，邮箱倒序存放在数据库，便可以通过前缀如“moc.qq@xxx”作为索引。
## 2. Myisam索引
###### Myisam索引图
![image](https://img-blog.csdnimg.cn/20200516185027444.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JfeF9w,size_16,color_FFFFFF,t_70)
## 三、事务
#### 1. 隔离级别及并发问题
- Read Uncommitted（读未提交），所有事务都可以看到其他未提交事务的执行结果，会产生**脏读**。
- Read Committed（读已提交），一个事务可以看见已提交的事务所做的修改。这样会有**不可重复读**（Nonrepeatable Read）问题，两次执行相同的查询，可能会得到不一样的结果，数据被其他已提交的事务修改过。
- Repeatable Read（可重复读），**mysql默认的事务隔离级别**，在同一个事务中多次读取同样记录的结果是一致的。但是会导致**幻读**，另一事务插入新行，再次读取该范围数据，会产生“幻行”。InnoDB通过MVCC解决了幻读问题。
    > mysql如何实现避免幻读？
    > - 在快照读情况下（```select ... from```），mysql通过mvcc来避免幻读。快照读是指读取该行之前的版本数据
    > - 在当前读（写锁，读取记录的最新版本）情况下，mysql通过临键锁next-key来避免幻读，所涉及的行都会被上锁，不允许其他事务修改
- Serializable（可串行化），最高的隔离级别，通过强制事务排序，是指不可能相互冲突。简单来说，是在读取的每一行数据上都加锁。
#### 2. MVVC
###### 概念
Multi-Version Concurrency Control，多版本并发控制协议，是为了控制数据库的并发问题、实现事务的隔离性，通过版本号，避免同一数据在不同事务间的竞争。  
优点：读不加锁，读写不冲突
###### 实现原理
InnoDB在每行数据都增加三个隐藏字段，==一个唯一行号，一个记录创建的版本号，一个记录回滚的版本号==。
1. DB_ROW_ID：行ID，MySQL的B+树索引特性要求每个表必须要有一个主键。如果没有设置的话，会自动寻找第一个不包含NULL的唯一索引列作为主键。如果还是找不到，就会在这个DB_ROW_ID上自动生成一个唯一值，以此来当作主键
2. DB_TRX_ID：事务ID，记录的创建该行记录的事务版本号
3. DB_ROLL_PTR：回滚指针，通过它可以将不同的版本串联起来，形成**版本链**。相当于链表的next指针  

![版本链](https://img-blog.csdnimg.cn/20200725023723438.png)  
**ReadView**，==select时创建的事务快照，将查询到的记录的事务ID与同时并发存在的事务ID数组进行比较，和min_id和max_id比较即可，决定哪个版本的数据可见。如果当前版本是不可见的，那就继续从版本链中通过回滚指针取上一版本的事务ID进行比较，直至找到可见的版本记录。==  
在读已提交的隔离级别下，每次select都会重新生成一份ReadView，活跃的事务组ID会发生变化；==在可重复读级别下，select得到的ReadView是不变的==，即活跃的事务ID数组不会改变。  
![ReadView](https://img-blog.csdnimg.cn/20200725024356278.png)  
[MVCC-可见性判断参考链接](https://blog.csdn.net/weixin_30342639/article/details/107552255)  
> 为什么 select count(*) from t，在InnoDB引擎中比MyISAM慢？
> - 在MyISAM存储引擎中，把表的总行数存储在磁盘上，当执行select cout() from t时，直接返回总数据的数量；  
在InnoDB 存储引擎中，并没有存储总行数在磁盘上。又因为MVCC的原因，查询到的每一行记录都需要判断否是可见；  
> - 在带where条件时，则速度不一定。

#### 3. undo log 
###### 概念
undo log主要记录的是数据的逻辑变化，主要用于事务回滚和MVCC：  
- 如果因为某些原因导致事务失败或回滚了，可以借助该undo log进行回滚；
- 当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据是什么
###### delete/update内部机制
当事务提交时，innodb不会立即删除undo log，因为后续还可能会用到；但会将该事务对应的undo log放入删除列表，通过purge线程 删除。  
- delete操作实际上不会直接删除，而是将delete对象打上delete flag，标记为删除，最终的删除操作是purge线程完成的
- update分为两种情况：update的列是否是主键列  
    -  如果不是主键列，在undo log中直接反向记录是如何update的。即update是直接进行的
    -  如果是主键列，update分两部执行：先删除该行，再插入一行目标行
#### 4. redo log 
###### 概念
redo log记录的是数据页的物理修改，用于崩溃恢复，确保事务的持久性。包括两部分：
- 内存中的日志缓冲(redo log buffer)，该部分日志是易失性的
- 磁盘上的重做日志文件(redo log file)，该部分日志是持久的。由缓冲区批量刷新至磁盘
###### 区别
数据库的ACID也是由此保证，MVCC保证隔离性I。
> - undo log负责原子性A，记录逻辑日志，保护事务在exception或手动rollback时可以回滚到历史版本数据  
> - redo log负责落盘式持久性D，记录物理日志，保证事务提交后新的数据不会丢失  
> - bin log记录对数据库执行更改的所有操作，负责副本式持久性，可以将主节点上的数据复制到从节点，主节点crash后业务可以正常运转
## 四、优化
#### 数据类型优化
- 字段选用小而简单的数据类型，因为他们占用更小的磁盘、内存和CPU缓存，并且处理时CPU周期也更少。
- 表字段不宜过多。mysql的存储引擎API需要在服务器层和存储引擎层之间通过行缓冲格式拷贝数据，然后在服务层将缓冲内容解码成各个列。
- 范式与反范式的选择。范式中只有很少或者没有重复数据，所以只需要修改更少的数据。但是通常需要进行表关联；反范式则可以很好的避免关联。
#### EXPLAIN
![image](https://img-blog.csdnimg.cn/c36819a169644130916afeafbeabf597.png#pic_center)
- id: select的查询序号，id越大越先执行
- selectType: 查询中select子句的类型
- table: 表示当前行访问的表
- type: 访问类型，表示MySQL在表中找到所需行的方式，这个字段直接反映我们SQL的性能是否高效
    -  const,system: 表只匹配了一行记录
    -  eq_ref: 唯一性索引扫描，多表联接中使用primary key或者 unique key作为关联条件
    -  ref: 使用普通索引
    -  index_merge: 使用了索引合并的优化方法，出现在两个索引的or条件查询
    -  range: 范围扫描通常出现在in(),between,>,<,>=等操作中。使用一个索引来检索给定范围的行
    -  index: 扫描全表索引
    -  all: 扫描全部数据行
    > 一般来说，得保证查询达到range级别，最好达到ref
- possible_keys: 可能会用到的索引
- key: 实际用到的索引
- key_len: 使用到索引的字节长度（联合索引可以由此判断用了哪几个字段）
- ref: 表示查找索引时使用的
- row: mysql估计要读取并检测的行数
- filtered：表示选取的行和读取的行的百分比
- extra: 展示额外信息
    - using index: 表示直接访问索引就能获取到所需的数据，如使用覆盖索引，聚簇索引
    - using where: 使用where语句来处理结果，并且查询的列为被索引覆盖
    - using index condition: 查询的列不完全被索引覆盖，用到了索引下推，但还是需要通过其他条件过滤
    - using temporary：mysql需要创建一张临时表来处理查询；这种情况需要优化
    - using filesort: 数据排序并非使用索引排序，使用的是外部排序。数据较小时从内存排序，否则在磁盘中完成排序
    - select tables optimized away：使用某些聚合函数（比如：max、min）来访问存在索引的某个字段  
#### SQL执行顺序：
1. FROM（将最近的两张表，进行笛卡尔积）—VT1
2. ON（将VT1按照它的条件进行过滤）—VT2
3. LEFT JOIN（保留左表的记录）—VT3
4. WHERE（过滤VT3中的记录）–VT4…VTn
5. GROUP BY（对VT4的记录进行分组）—VT5
6. HAVING（对VT5中的记录进行过滤，在聚合后对组记录进行筛选）—VT6
7. SELECT（对VT6中的记录，选取指定的列）–VT7
8. ORDER BY（对VT7的记录进行排序）–游标
9. LIMIT（对排序之后的值进行分页）
## 五、锁
#### 并发控制的思想
###### 乐观锁
会“乐观地”假定大概率不会发生并发更新冲突，访问、处理数据过程中不加锁，只有在更新数据时再判断是否和其他事务有冲突。可以根据版本号或时间戳判断是否有冲突。
###### 悲观锁
会“悲观地”假定大概率会发生并发更新冲突，访问、处理数据前就加锁，在整个数据处理过程中锁定数据，阻塞其他人拿到锁。mysql的行锁，表锁，读锁，写锁，都是在操作之前上锁；java中的```synchronized```,```reentrantLock```等独占锁都是悲观锁思想的实现。
#### 行锁
处理并发能力强，可能发生死锁
###### 共享锁（读锁）
允许事务读一行数据，阻止其他事务获得相同数据集的**排他锁**，但可以加共享锁，==即其他事务能读，不能写==  
加锁方式：```SELECT ... LOCK IN SHARE MODE```
###### 排他锁（写锁）
允许持有排他锁的事务读写数据，阻止其他事务获取改数据的共享锁和排他锁，==即其他事务不能读，不能写==
加锁方式：```SELECT ... FOR UPDATE```，```update```，```delete```，```insert```  
> select ... form并不会给数据加锁
###### 行锁的三种算法
- record lock 记录锁，单个行记录上锁
- gap lock 间隙锁，锁定一个范围，但不包含记录本身，开区间。对于普通索引，左闭右开[3,5)
- **next key lock** 临键锁，gap lock + record lock，锁定范围左开右闭 (3,5] ，并且锁定记录本身  
    - 当查询的索引含有唯一索引时，InnoDB会对Next-Key Lock进行优化，降级为record lock，即仅锁住索引本身；  
    - 在使用唯一索引（unique index）精确匹配（=）且不存在记录时，退化为间隙锁；  
    - 在使用唯一索引（unique index）范围匹配（>或<），不会退化；
#### 表锁
整个表上锁，并发处理能力弱，不会发生死锁
###### 意向锁
当有事务A有行锁时，MySQL会自动为该表添加意向锁，事务B如果想申请整个表的写锁，那么不需要遍历每一行判断是否存在行锁，而直接判断是否存在意向锁
###### 意向共享锁(IS锁)
一个事务在获取（任何一行/或者全表）S锁之前，一定会先在所在的表上加IS锁
###### 意向排他锁(IX锁)
一个事务在获取（任何一行/或者全表）X锁之前，一定会先在所在的表上加IX锁
###### 自增锁
事务插入自增类型的列时获取自增锁，如果一个事务正在往表中插入自增记录，所有其他事务的插入必须等待
#### 死锁
在并发系统中不同线程出现循环资源依赖，涉及的线程都在等待别的线程释放资源时，就会导致这几个线程都进入无限等待的状态，称为死锁
## 六、主从复制
![image](https://img2018.cnblogs.com/blog/1606768/201904/1606768-20190408141304873-785770801.png)
#### 流程
1. master在每个事务更新数据完成之前，将该操作记录串行地写入到binlog文件中。
2. salve开启一个I/O Thread，该线程在master打开一个普通连接，主要工作是binlog dump process。I/O线程最终的目的是将这些事件写入到中继日志relay-log中。
3. slave SQL Thread会读取中继日志，并顺序执行该日志中的SQL事件，从而与主数据库中的数据保持一致。
#### 基本配置
###### 主库 /etc/my.cnf
```
log-bin = mysql-bin   //[必须]启用二进制日志
server-id = 1      //[必须]服务器唯一ID，默认是1
binlog-do-db = flexible-platform //[可选]要同步的数据库，不设置则默认全部
//只允许192.168.0.104使用root2，且密码为"123456"连接主库做数据同步；若要所有网段则设置root2@'%'；部分网段：repl@'192.168.0.%'
//可以
mysql > grant replication slave,replication client on *.* to root2@'192.168.0.104' identified by "123456";
```
![image](https://img-blog.csdnimg.cn/d0d132558845464cb42a50bfc4f09219.png#pic_center)
###### 从库
```
log-bin = mysql-bin  
server-id = 2
relay-log=/var/lib/mysql/mysql-relay-bin
log_slave_updates=1
read-only=1
replicate-do-table = flexible-platform.automation_status #指定同步库里的指定表
replicate-ignore-db=yzf_biz #忽略指定数据库
#slave-skip-errors=1062,1053,1146  #跳过指定error no类型的错误
```
![image](https://images2015.cnblogs.com/blog/771870/201603/771870-20160309163146257-1219019147.png)  

[修改my.cnf后，服务器需要重启](https://www.cnblogs.com/purple5252/p/11904284.html)
```
mysql > start slave;
mysql > show slave status \G;
```
![image](https://img-blog.csdnimg.cn/f02df34ff86f4f27804a125455ad68d4.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzQwNTczMw==,size_16,color_FFFFFF,t_70#pic_center)
#### 多主一从

```
#多主一从需要指定channel
change master to ... for channel 'flexible-platform';
mysql-> change master to master_host='172.23.0.135', master_port=5688, master_user='yunlintong', master_password='123456', master_log_file='mysql-bin.000086',master_log_pos=617604 
for channel 'yunlintong';
stop/start slave for channel 'flexible-platform';
#删除指定channel
reset slave all for channel 'fintax_flexible_platform';
```
> 主从的GTID设置要一致，不一致解决方法：https://blog.csdn.net/diaozhou2796/article/details/102039165
#### Cannal
#### otter


## 七、高级特性
#### 分区
###### 概念
对于用户而言，分区表是一个独立的逻辑表，但是++底层根据一定规则，将一张表其分成了多个物理子表++。主要目的是将数据按照一个较粗的粒度分在不同的表中，这样可以将相关的数据存放在一起，而且如果想一次性删除整个分区的数据也很方便。  

 创建表时使用**partition by**子句定义每个分区存放的数据，执行查询时，优化器会根据分区定义过滤那些没有我们需要的数据的分区，这样查询只需要查询所需数据所在的分区即可。分区有多种类型：range, list, hash, key
 
```
create table test_list_partiotion(
    id int auto_increment,
    data_type tinyint,
    primary key(id,data_type)
)partition by list(data_type)(
    partition p0 values in (0,1,2,3,4,5,6),
    partition p1 values in (7,8,9,10,11,12),
    partition p2 values in (13,14,15,16,17)
);
```
#### 视图
###### 概念
视图本身是个临时表，不存放任何数据。视图本质是一条select语句查询结果  
###### 作用
如果需要经常执行某项复杂查询，可以基于这个复杂查询建立视图，此后查询此视图即可，简化复杂查询
#### 存储过程
可以理解是数据库SQL语音层面的代码封装与重用。  
存储过程执行的速度要比客户端程序快得多，因为它无须++网络通信开销、解析开销和优化器开销。++

#### 触发器
是一种特殊的存储过程，主要通过事件触发，而不是主动调用。  
可以在执行insert、update、delete的时候执行特定的操作。触发器可以减少客户端和服务器之间的通信。 
#### 分表 ShardingSphere
###### [ShardingSphere入门课程](https://www.bilibili.com/video/BV1KK4y147pD)

