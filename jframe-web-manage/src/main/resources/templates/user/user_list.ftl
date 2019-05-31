<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title></title>
    <#include "include.ftl"/>
    <style type="text/css">
        .red {
            background-color: red !important;
            color: white !important;;
        }
    </style>
    <!--解决表格标题内容居中问题-->
    <style>
        table tr td{
            text-align: center!important;
        }
        table tr th{
            text-align: center!important;
        }
    </style>
</head>

<body class="gray-bg">
<div class="ibox float-e-margins">
    <div class="ibox-content">
        <div class="row">
            <div class="col-md-13">
                <form class="form-inline" method="post" id="queryForm">
                    <div class="col-md-7">
                        <label>用户信息:<input placeholder="昵称/手机号/姓名" type="text" name="keywords" class="form-control input-sm" style="width:180px;"></label>
                        <label>注册时间:
                            <input type="text" class="form-control input-sm" id="startDate" name="startDate" placeholder="开始时间">
                            <input type="text" class="form-control input-sm" id="endDate" name="endDate" placeholder="结束时间">
                        </label>
                    </div>
                    <div class="col-md-5 text-right">
                        <button type="button" class="btn btn-primary btn-sm" data-open="modal" data-width="800px" data-height="500px" href="/admin/user/userDetail" id="btn-add"><i class="fa fa-plus"></i> 添加</button>
                        <button type="button" id="search" class="btn btn-sm btn-info"><i class="fa fa-search"></i>查询</button>
                        <button type="button" id="clear" class="btn btn-sm btn-info"><i class="fa fa-remove"></i>清空</button>
                        <button type="button" id="export" class="btn btn-sm btn-info"><i class="fa fa-share"></i>导出Excel</button>
                        <button type="button" id="export2" class="btn btn-sm btn-info"><i class="fa fa-share"></i>导出Pdf</button>
                        <button type="button" class="btn btn-danger btn-sm" id="btn-del"><i class="fa fa-remove"></i> 批量删除</button>
                    </div>
                </form>
            </div>
        </div>
        <table id="table" class="table table-striped table-bordered table-hover" width="100%"></table>
    </div>
</div>

<script type="text/javascript">
    $.getScript("/static/common/js/enums.js");
    var tables;
    $(function () {
        var columns = [
            CONSTANT.COLUMN.CHECKALL,
            {
                title: '序号',
                data: null,
                render:function(data,type,row,meta) {
                    return meta.row + 1 +
                        meta.settings._iDisplayStart;
                }
            },
            {title: "ID", data: "id"},
            {title: "昵称", data: "nickname", defaultContent: "--"},
            {title: "手机号", data: "phone", defaultContent: "--"},
            {title: "邮箱", data: "email", defaultContent: "--", orderable: false},
            {title: "真实姓名", data: "realname", defaultContent: "--", orderable: false},
            {title: "头像", data: "avatar", orderable: false, render: CONSTANT.RENDER.AVATAR},
            {title: "注册时间", data: "createTime", defaultContent: "--"},
            {title: "是否冻结", data: "locked",
                render: function (data, type, full, callback) {
                    if (full.locked=='1') {
                        return "是";
                    } else {
                        return "否";
                    }
                }
            },
            {
                title: "操作", data: null, orderable: false,
                render: function (data, type, full, callback) {
                    // btn-edit btn-del
                    var btns = CONSTANT.BUTTON.EDIT("/admin/user/userDetail?userId=" + full.id);
                    if (full.forbided=='1') {
                        btns += CONSTANT.BUTTON.ENABLE();
                    } else {
                        btns += CONSTANT.BUTTON.DISABLE();
                    }
                    btns += CONSTANT.BUTTON.DELETE();
                    return btns;
                }
            }
        ]
        var $table = $('#table');
        // 默认查询表单id为queryForm
        tables = $table.DataTable($.extend(true, {}, CONSTANT.DEFAULT_OPTION, {

            //dom: '<"top">rt<"bottom"flpi><"clear">',
          // dom: '<"top"fli>rt<"bottom"p><"clear">',
            columns: columns,
            ajax: function (data, callback, settings) {
                CONSTANT.AJAX("/admin/user/userListData", [[2, "u.id"], [3, "u.nickname"], [4, "u.phone"], [8, "u.create_time"], [9, "u.locked"]], data, callback, settings);
            },
            columnDefs: [
                CONSTANT.BUTTON.CHECKBOXS,
                {"width": "5%", "targets": 1} // 列宽 | className:""
            ],
            drawCallback: function (settings) {
                $(":checkbox[name='check-all']").prop("checked", false);
                /* 自增序列
                    this.api().column(0).nodes().each(function (cell, i) {
                        cell.innerHTML = i + 1;
                    });
                */
                appendSkipPage();
            }
        }));

        $("#search").on("click", function () {
            tables.draw();// 查询后不需要保持分页状态，回首页
        });

        // 销毁tables实例
        // tables.destroy();
        $("#btn-test").on("click", function () {
            tables.destroy();
            $('#table').html('');
        });

        $("#export").click(function () {// 导出到Excel
            var formData = $("#queryForm").serialize();
            location.href = '/admin/user/exportUserExcel?' + formData;
        });
        $("#export2").click(function () {// 导出到Pdf
            var formData = $("#queryForm").serialize();
            location.href = '/admin/user/exportUserPdf?' + formData;
        });

        $("#btn-del").click(function () {// 批量删除
            var arrItemId = [];
            $("tbody :checkbox:checked", $table).each(function (i) {
                var item = tables.row($(this).closest('tr')).data();
                arrItemId.push(item.id);
            });
            if(arrItemId.length<1){
                showMsg("请选择至少一个用户", 2);
            }else{
                //
               // $(this).closest('tr').remove(); // 删除
                layerConfirm('是否删除选中用户', "/admin/user/batchDeleteUser?userIds=" + arrItemId.join(","), function () {
                    reload();
                });
            }
        });

        // 单元格事件绑定
        $table.on("change", ":checkbox", function () {
            if ($(this).is("[name='check-all']")) {// 全选
                $(":checkbox", $table).prop("checked", $(this).prop("checked"));
            } else {// 一般复选
                var checkbox = $("tbody :checkbox", $table);
                $(":checkbox[name='check-all']", $table).prop('checked', checkbox.length == checkbox.filter(':checked').length);
            }
        }).on("click", ".btn-edit", function () {// 点击编辑按钮
            //var item = tables.row($(this).closest('tr')).data();
        }).on("click", ".btn-enable", function () {// 点击启用按钮
            var item = tables.row($(this).closest('tr')).data();
            //$(this).closest('tr').remove();
            layerConfirm('确定要启用该用户吗', "/admin/user/userEnable?userId=" + item.id+"&forbided=0", function () {
                reload();
            });
        }).on("click", ".btn-disable", function () {// 点击禁用按钮
            var item = tables.row($(this).closest('tr')).data();
            //$(this).closest('tr').remove();
            layerConfirm('确定要禁用该用户吗', "/admin/user/userEnable?userId=" + item.id+"&forbided=1", function () {
                reload();
            });
        }).on("click", ".btn-del", function () {// 点击删除按钮
            var item = tables.row($(this).closest('tr')).data();
            //$(this).closest('tr').remove();
            layerConfirm('确定要删除该用户吗', "/admin/user/deleteUserByuserId?userId=" + item.id, function () {
                reload();
            });
        });

        // 行点击事件
/*        $('#table tbody').on('click', 'tr',function() {
            $(this).closest('tr').toggleClass("red");
        });*/

        $("#clear").click(function () {
            //清空form
            $("#queryForm")[0].reset();
        });

        datePicker('#startDate,#endDate', "yyyy-mm-dd");
    });


    // 不改变当前页
    function reload() {
        tables.draw('page');
    }

    function dealUser(data) {
        console.log("选择的数量为：" + data.length);
        console.log(data);
    }

    function appendSkipPage() {
        var table = $("#table").dataTable();
        var template =
            $("<li class='paginate_button active'>" +
                "   <div class='input-group' style='margin-left:3px;'>" +
                "       <span class='input-group-addon' style='padding:0px 8px;background-color:#fff;font-size: 12px;'>跳转页</span>" +
                "       <input type='text' class='form-control' id='searchPage' style='text-align:center;padding: 8px 2px;height:30px;width:40px;' onkeydown='onKeyDown(event)' onkeyup=\"value=value.replace(/^(0+)|[^\\d]+/g,'')\"/>" +
                "       <span class='input-group-addon active' style='padding:0px 8px;'><a href='javascript:void(0)'> Go </a></span>" +
                "   </div>" +
                "</li>");

        template.find("a").click(function () {
            var pn = template.find("input").val();
            if (pn > 0) {
                --pn;
            } else {
                pn = 0;
            }
            table.fnPageChange(pn);
        });
        $("ul.pagination").append(template);
    }


    function onKeyDown(event){
        var e = event || window.event || arguments.callee.caller.arguments[0];
        if(e && e.keyCode==27){ // 按 Esc
            //要做的事情
        }
        if(e && e.keyCode==113){ // 按 F2
            //要做的事情
        }
        if(e && e.keyCode==13){ // enter 键
            var page = $("#searchPage").val();
            page = parseInt(page) || 1;
            page = page - 1;
            //调转到指定页面索引 ，注意大小写
            var oTable = $('#table').dataTable();
            oTable.fnPageChange(page);
        }
    }

</script>
</body>
</html>