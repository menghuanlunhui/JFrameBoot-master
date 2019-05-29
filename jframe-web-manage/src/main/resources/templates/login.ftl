<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录</title>
    <script type="text/javascript">if (window != top) {parent.location.reload();}</script>
    <link rel="stylesheet" href="/static/library/plugins/camera/camera.css"/>
    <#include "include.ftl"/>
</head>

<body class="gray-bg">
<div id="templatemo_banner_slide" class="container_wapper">
    <div class="camera_wrap camera_emboss" id="camera_slide">
        <div data-src="/static/theme/images/login/3.jpg"></div>
        <div data-src="/static/theme/images/login/2.jpg"></div>
        <div data-src="/static/theme/images/login/4.jpg"></div>
        <div data-src="/static/theme/images/login/5.jpg"></div>
    </div>
</div>
<div class="middle-box text-center">
    <div>
        <h1 class="logo-name"> 平台管理中心 </h1>
        <h5>version：${version}</h5>

        <form style="opacity: .8;" role="form" method="post" id="loginForm">
            <div class="form-group">
                <div class="input-group">
                    <span class="input-group-addon"><i class="fa fa-user"></i></span>
                    <input type="text" class="form-control" placeholder="用户名" name="username" id="username" value="">
                </div>
            </div>
            <div class="form-group">
                <div class="input-group">
                    <span class="input-group-addon"><i class="fa fa-key"></i></span>
                    <input type="password" class="form-control" placeholder="密码" name="password" id="password" value="">
                </div>
            </div>
            <div class="form-group">
                <div class="row">
                    <div class="col-md-7 col-sm-7 col-xs-7">
                        <div class="input-group">
                            <span class="input-group-addon"><i class="fa fa-bars"></i></span>
                            <input type="text" class="form-control" id="validNum" name="validNum" placeholder="验证码" value="">
                        </div>
                    </div>
                    <div class="col-md-5 col-sm-5 col-xs-5">
                        <img src="/getValidCode" onclick="changeNum(this)" id="validImg">
                    </div>
                </div>
            </div>
            <div style="margin-top:-5px;">
                　　<label><input id="remember" style="float: left;" type="checkbox"><span style="color:#ffffff">记住用户名和密码</span></label>
            </div>
            <button type="submit" class="btn btn-danger block full-width">登 录</button>
            <#--<h5><a href="/">网站首页</a></h5>-->
        </form>
    </div>
</div>
<script src="/static/library/jquery/jquery.easing.min.js"></script>
<script src="/static/library/plugins/camera/camera.min.js"></script>
<script src="/static/library/plugins/camera/templatemo_script.js"></script>
<script src="/static/common/js/gt.js"></script>
<script>
    window.onload = function(){
        //分析cookie值，显示上次的登陆信息
        var oRemember = document.getElementById("remember");
        if(getCookie("cyusername") && getCookie("cypassword")){
            $("#username").val(getCookie("cyusername"));
            $("#password").val(getCookie("cypassword"));
            oRemember.checked = true;
        }
    };

    $(function () {
        $('#loginForm').bootstrapValidator({
            fields: {
                username: {
                    validators: {
                        notEmpty: {
                            message: '账号不能为空'
                        }
                    }
                },
                password: {
                    validators: {
                        notEmpty: {
                            message: '密码不能为空'
                        }
                    }
                },
                validNum: {
                    validators: {
                        notEmpty: {
                            message: '验证码不能为空'
                        }
                    }
                }
            }
        }).on('success.form.bv', function (e) {
            e.preventDefault();
            var $form = $(e.target);
            Ajax.ajax({
                url: '/admin/dologin',
                params: $form.serialize(),
                success: function (data) {
                    if (data.code == 0) {//code为0时表示登陆成功
                        //账号和密码都有时根据后台返回的登录状态success或者failure做判断，当是success时添加以下代码
                        if(remember.checked){//记住密码
                            setCookie('cyusername',$("#username").val(),7); //保存帐号到cookie，有效期7天
                            setCookie('cypassword',$("#password").val(),7); //保存密码到cookie，有效期7天
                        }else{//取消记住密码
                            delCookie('cyusername');
                            delCookie('cypassword');
                        }
                        layer.msg(data.msg, {time: 1000, icon: 1, offset: 0}, function () {
                            jumpUrl("/admin/index");
                        });
                    } else {
                        layer.msg(data.msg, {icon: 2, offset: 0});
                        $("#validImg").attr("src", "/getValidCode?" + Math.floor(Math.random() * 10000));
                    }
                }
            });
        });
    });
    //设置cookie
    function setCookie(name,value,day){
        var date = new Date();
        date.setDate(date.getDate() + day);
        document.cookie = name + '=' + value + ';expires='+ date;
    };
    //获取cookie
    function getCookie(name){
        var reg = RegExp(name+'=([^;]+)');
        var arr = document.cookie.match(reg);
        if(arr){
            return arr[1];
        }else{
            return '';
        }
    };

    //删除cookie
    function delCookie(name){
        setCookie(name,null,-1);
    };
</script>
</body>
</html>