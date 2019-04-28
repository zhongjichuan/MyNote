1. 创建文件时可以使用临时文件，下载完成后再将其Copy,删除。
	DirectoryInfo类，公开用于创建、移动和枚举目录和子目录的实例方法。 此类不能被继承。如：GetFiles()，MoveTo()...
	FileInfo类：提供用于创建、复制、删除、移动和打开文件的属性和实例方法，并且帮助创建 FileStream 对象。 此类不能被继承。如：FileStream.CopyTo(String, Boolean)...
2. 使用Newtonsoft.Json及对象在Josn中的序列化传输。
	后台：
	DataTable data = departmentInfoBusiness.GetEmployeeList(Db, where);
    string JsonData = "";
    JsonData = JsonConvert.SerializeObject(data);//使用Newtonsoft.Json将DataTable序列化;Newtonsoft.Json，是.Net中开源的Json序列化和反序列化工具
	return new ResultDTO()
            {
                success = true,
                data = JsonData
            };
	前台：
	var btnInquiry_Click = function () {
    var departmentType = $ddlType.val();
    var option = {
        "success": function (result) {
            if (result.success) {
                var dataTable = eval(result.data);//eval() 函数可计算某个字符串，并执行其中的的 JavaScript 代码。此处将字符串转换为Json对象。
                $grdParameter.bootstrapTable("load", dataTable);
                $grdParameter.bootstrapTable("resetWidth");
            } else {
                $.dialog.showMessage({
                    title: _CurrentLang["lang_error"],
                    content: _CurrentLang["err_operation_failed"] 
                });
            }
        }
    };
    $.callWebService("../Service/DepartmentInfoService.asmx/GetAllEmployee", { "departmentType": departmentType }, option);
    return false;
	};
3. GridView.Rows[i].Cell[i].Text;gridview.Rows[].Cells[].Controls[];rows代表第几行,Cells代表第几个表格<td></td>,  Controls代表控制器
4. DTO,数据传输对象。
5. this.QButtonEdit.Attributes.Add("onclick", "javascript:return OpenProjectBasicInfoEdit('" + this.m_ProjectID + "', '" + this.m_UserName + "');");
6. Sql语句varchar或nvarchar字段条件前加N的话是对这个字段进行Unicode编码，可以解决中文乱码问题，但是性能有所下降。
7. JS中opener，为父窗口的对象，可在在子窗口中操作父窗口对象。
	子窗口刷新父窗口等多种方法。
	<script> window.opener.location.reload();;window.opener=null;window.close();</script>
8. Oracle，C:\oracle\ora92\network\ADMIN\tnsnames.ora ，用描述符来来说明Oracle的连接信息
9. 在程序界面上改变textbox.text的值，能触发textchanged事件，但是在后台改变（button按钮事件中给textbox.text赋新值）不能触发textchanged事件。
10. 在sql语句中，还有count(1),count(某字段)的用法，他们的执行效率都比count（*）高。
11. IList不能序列化，但是可以使用List作为WebService的参数。这样在客户端生成的参数类型是数组类型.但是不建议，可以使用字符串传参，在WS中解析。
12. WebReference 可以被 .net1.1 或.net2.0的客户端调用。ServiceReference生成的代理只能被.net3.0+的客户端调用。
13.	检索 COM 类工厂中 CLSID 为 {00024500-0000-0000-C000-000000000046} 的组件时失败，原因是出现以下错误: 80070005。
	ASP.NET 未被授权访问所请求的资源....对于电脑上的一些程序，IIS用户可能没有访问权限，可运行dcomcnfg（64）
	或comexp.msc -32 （32位，Word,Excel都在这里面），对这些组件添加用户权限（最大权限Everyone）.
14. .net要在word文档指定地方修改信息，可以使用书签Bookmarks。using Word=Microsoft.Office.Interop.Word;
	例： BookMarkName = "BookMarkFormName";
		if (doc.Bookmarks.Exists(BookMarkName.ToString()))
		{
			doc.Bookmarks.get_Item(ref BookMarkName).Select();
			app.Selection.TypeText(strFormName);
		}
15. .net中操作Excel文件，Excel中没有书签这一功能，可以	
	例：excelApp = new Microsoft.Office.Interop.Excel.Application();
		workbook = (Microsoft.Office.Interop.Excel.Workbook)(excelApp.Workbooks.Add(strFilePath));
		workbook = excelApp.Workbooks.Open(strFilePath);
		sheet = (Microsoft.Office.Interop.Excel.Worksheet)workbook.ActiveSheet;
		range = sheet.get_Range(sheet.Cells[2, 1], sheet.Cells[2, 1]);
		range.Value2 = "From No:" + strDocNo + "(" + strApproveDate + ")";
		workbook.Save();
		workbook.Close();
		excelApp.Quit();
16. ASP.NET网页运行后，服务器控件会随机生成客户端id，获取控件的方法
	1. $("#<%=txtID.ClientID%>").val();
	2. $("input[id*=txtID]").val();
	3. $("*[id$=txtID]").val();
17. 1. selectedIndex——指的是dropdownlist中选项的索引，为int，从0开始，可读可写
	2. selectedItem——指的是选中的dropdownlist中选项，为ListItem，只读不写
	3. selectedValue——指的是选中的dropdownlist中选项的值，为string， 只读不写
	4. selectedItem.Text——指的是选中的dropdownlist中选项的文本内容，与selectedItems的值一样为string，可读可写
	5. selectedItem.value——指的是选中的dropdownlist中选项的值，与selectedValue的值一样，为string，可读可写
18. 自定义控件事件的使用。在自定义控件中声明委托，事件，在控件的触发函数中使用事件。
	在使用自定义控件时，在前台指明控件事件，后台定义该事件。这样触发控件函数是就会执行事件。
19. onchange事件是指控件获得焦点时和失去焦点时，控件内容发生变化，否则不触发onchange事件。
	在前台JS中给文本框赋值，是不会触发该事件的。可以手动触发，如： document.getElementById("id").onChange();
20. onbeforeunload 事件在即将离开当前页面（刷新或关闭）时触发。
	该事件可用于弹出对话框，提示用户是继续浏览页面还是离开当前页面。
	当页面有Ajax刷新使可能触发该事件，可以使用updatePanel实现页面的局部刷新。
	例： window.onbeforeunload = function()
        {
            if (document.getElementById("NeedSave").value == "true")
            {
                window.event.returnValue = "It's not saved yet.";
            }
        }
21. 使用jquery.autocomplete实现文本框边输入边过滤
	<script type="text/javascript" src="../../../JavaScript/jquery.js"></script>  
    <link type="text/css" rel="stylesheet" href="../../../JavaScript/jquery-ui-192.css" />  
    <script type="text/javascript" src="../../../JavaScript/jquery-ui.js"></script> 	 
    $(function () {
             $("#TextBoxMarketingName").autocomplete({
                 minLength: 1,   // 设置搜索的关键字最小长度  
                 max: 10,        // 下拉项目的个数  
                 source: function (request, response) {
                     $.ajax({
                         type: "POST", // 通过 request.term 可以获得文本框内容  
                         url: "ProjectMenberHandler.ashx?keyword=" + request.term, // 使用 Handler.ashx?keyword= 传参  
                         contentType: "application/json; charset=gb2312",
                         dataType: "json",
                         success: function (data) {
                             // jQuery.map(array, callback) :将一个数组中的元素转换到另一个数组中。  
                             // 下面就是把数组["value1", "value2",...]转换为[{value:"value1"}, {value:"value2"},...]  
                             response($.map(data, function (item) {

                                 return { value: item };

                             }));
                         },
                         error: function () {
                             alert("ajax请求失败");
                         }
                     })
                 },
                 close: function () {//文本框提示（菜单）关闭时触发。
                     $("#ButtonMarketingName").click();
                 }

             })
         });
    public class ProjectMenberHandler : IHttpHandler
    {

        /// <summary>      
        /// 根据关键字过滤内容    
        /// </summary>      
        /// <param name="keyword">关键字</param>     
        /// <returns>过滤数组</returns>      
        private string[] GetFilteredList(string keyword)
        {
            List<string> resultList = new List<string>();
            SqlDataReader dr = null;
            string sql = @" SELECT distinct MarketingName FROM [PDM].[dbo].[pdm_bproject] 
	                            where productline in (select productline FROM [PDM].[dbo].[pdm_bproject]) 
		                            and Upper(marketingName) like '%" + keyword + @"%'
		                            order by MarketingName";
            dr = BenQ.PDM.Common.SQLHelper.ExecuteReaderQuery(sql);
            while (dr.Read())
            {
                resultList.Add(dr[0].ToString());// 每次读取后数据前推,因此只需读首行即可  
            }

            // 过滤关键字  
            int maxlist = 10;// 下拉列最大显示数  
            List<string> filteredList = new List<string>();
            foreach (string sResult in resultList)
            {
                // 判断是否包含关键字，然后加入到过滤后的集合中。     
                if (sResult.ToUpper().Contains(keyword.ToUpper()) && maxlist != 0)
                {
                    filteredList.Add(sResult);
                    maxlist--;
                }
            }
            // 返回数组，以便后面能序列化为JSON格式数据       
            return filteredList.ToArray();
        }  

        public void ProcessRequest(HttpContext context)
        {
            string keyword = context.Request.QueryString["keyword"];// 获取参数字符串  
            if (keyword != null)
            {
                JavaScriptSerializer serializer = new JavaScriptSerializer();
                // 通过 JavaScriptSerializer 对象的 Serialize 序列化为["value1","value2",...]的字符串      
                string jsonString = serializer.Serialize(GetFilteredList(keyword));
                context.Response.Write(jsonString); // 返回客户端json格式数据         
            }  
        }

        public bool IsReusable
        {
            get
            {
                return false;
            }
        }
    }

22. ibatis中如果查询的字段多了，resultMap对应的字段少了，不会报错，只是查询后后台取的的数据这个字段为NULL值
	如果resultMap多了，查询数据的时候没有查询出来这个数据，那么此时就会报错,比如说我查询的时候没有查询name这个字段，但是resultMap里面对应的有这个字段，则会报错
	可以新增一个ResultMap,增加所需属性，使用resultMap中的extends继承上一个ResultMap.
	例：<resultMap id="PdmBprojectResultAddEstimatedfinishdate" extends="PdmBprojectResult" class="PdmBproject">
			<result property="Estimatedfinishdate" column="estimatedfinishdate" type="datetime" dbType="datetime"/>
		</resultMap>
	或<result property="xx" select="".../>
23. ibatis中使用like模糊查询,select  *  from table1 where name like '%$name$%',用$代替%
24. Button.Attributes.Add("javascript事件","javascript语句");
25. merge语法是根据源表对目标表进行匹配查询，匹配成功时更新，不成功时插入。如：dataSet.Merge()
26. 文件上传服务器控件FileUpload，默认情况下可上传的最大文件为4M，如果要改变可上传文件大小限制，那么我们可以在web.	config中的httpRuntime元素中添加maxRequestLength属性设置大小，同时为了支持大文件上传超时可以添加executionTimeout属性设	置超时时间。
	例：<httpRuntime maxRequestLength="40690" useFullyQualifiedRedirectUrl="true" executionTimeout="6000"
27. <input type="file" id="FileCheckIn" runat="server"/>  后台使用FileCheckIn.PostedFile.FileName获得文件名称。
	PostedFile提供对客户端已上载的单独文件的访问。
	<asp:FileUpload>
28. Response.Flush() 将缓存中的内容立即显示出来
	Response.End()  缓冲的输出发送到客户端  停止页面执行
	在导出Excel文件时，文件中包含所需内容和前台代码，原因是Response.End()
29. 何为 “嵌入互操作类型” ？？？
	1. ”嵌入互操作类型”中的嵌入就是引进、导入的意思，类似于c#中using，c中include的作用，目的是告诉编译器是否要把互操作类型引入。   “互操作类型”实际是指一系列Com组件的程序集，是公共运行库中库文件，类似于编译好的类，接口等。
	“嵌入互操作类型”设定为true，实际上就是不引入互操作集（编译时候放弃Com程序集），仅编译用户代码的程序集。而设定为false的话，实际就是需要从互操作程序集中获取 COM 类型的类型信息。
30. GridView没有绑定数据源，则GridView无法在页面显示出来。
31. SQL SERVER字符串函数LTRIM()/RTRIM(),分别为去除字符串左边多余的空格函数和去除字符串右边多余的空格函数 
32. 采用debugCode的方法可以快速定位到bug处。
	不抛出ex.Message,使用ex.tostring()会显示出错的行数。

 