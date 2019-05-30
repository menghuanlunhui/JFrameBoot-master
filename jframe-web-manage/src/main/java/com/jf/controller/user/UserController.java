package com.jf.controller.user;

import com.github.pagehelper.PageInfo;
import com.jf.common.BaseController;
import com.jf.controller.view.ViewPDF;
import com.jf.controller.view.ViewExcel;
import com.jf.database.model.User;
import com.jf.database.model.excel.UserModel;
import com.jf.encrypt.PasswordUtil;
import com.jf.entity.ResMsg;
import com.jf.entity.enums.ResCode;
import com.jf.service.system.SystemService;
import com.jf.service.user.UserService;
import com.jf.string.StringUtil;
import com.jf.annotation.AuthPassport;
import com.jf.poi.ExcelReaderXSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员模块
 *
 * @author rick
 */
@Controller
@RequestMapping("/admin/user")
public class UserController extends BaseController {

    @Resource
    private UserService userService;
    @Resource
    private SystemService systemService;

    /**
     * 用户列表
     *
     * @return
     */
    @RequestMapping("/userList")
    @AuthPassport(right = false)
    public String userList(String mode) {
        if (mode != null) { // 模态框
            return "user/user_list_mode";
        }
        return "user/user_list";
    }

    /**
     * 用户列表数据
     *
     * @param condition
     * @return
     */
    @RequestMapping("/userListData")
    @ResponseBody
    @AuthPassport
    public PageInfo userListData(User condition) {
        condition.setDeleted("0");//未删除
        return userService.findUserByPage(condition);
    }

    /**
     * 禁用和启用用户
     *
     * @param userId
     * @return
     */
    @RequestMapping("/userEnable")
    @AuthPassport
    @ResponseBody
    public ResMsg userEnable(Long userId, String forbided,HttpServletRequest request) {
        if (userId == null) {
            return new ResMsg(ResCode.CODE_22.code(), ResCode.CODE_22.msg());
        }
        User condition = new User();
        condition.setId(userId);
        condition.setForbided(forbided);
        if (userService.updateUser(condition) > 0) {
            systemService.addAdminLog(request, "禁用/启用用户", "id=" + userId);
            return new ResMsg(ResCode.OPERATE_SUCCESS.code(), ResCode.OPERATE_SUCCESS.msg());
        }
        return new ResMsg(ResCode.OPERATE_FAIL.code(), ResCode.OPERATE_FAIL.msg());
    }

    /**
     * 会员新增/编辑
     *
     * @param user model
     * @return
     */
    @RequestMapping("/userEdit")
    @AuthPassport
    @ResponseBody
    public ResMsg userEdit(@Valid User user, BindingResult br, HttpServletRequest request) {
        if (br.hasErrors()) {
            return new ResMsg(1, br.getFieldError().getDefaultMessage());
        }
        String nickname = user.getNickname();
        String phone = user.getPhone();
        String email = user.getEmail();
        Long userId = user.getId();
        String password = user.getPassword();
        if(!"".equals(password) && password!=null){
            user.setPassword(PasswordUtil.MD5Encode(password));
        }
        if (userId != null) {
            User t = userService.findUserById(userId);
            if (!phone.equals(t.getPhone())) {
                if (userService.findUserCountByPhone(phone) > 0) {
                    return new ResMsg(3, "手机号已存在");
                }
            }
            if (!email.equals(t.getEmail())) {
                if (userService.findUserCountByEmail(email) > 0) {
                    return new ResMsg(4, "邮箱已存在");
                }
            }
            int res = userService.updateUser(user);
            if (res > 0) {
                systemService.addAdminLog(request, "编辑用户", "phone=" + phone);
                return new ResMsg(ResCode.UPDATE_SUCCESS.code(), ResCode.UPDATE_SUCCESS.msg());
            }
            return new ResMsg(ResCode.UPDATE_FAIL.code(), ResCode.UPDATE_FAIL.msg());
        }else{//新增
            User userInfo = userService.findUserByPhone(phone);
            if (userInfo!=null) {
                return new ResMsg(3, "手机号已存在");
            }else{
               int i = userService.insertUser(user);
               if(i>0){
                   return new ResMsg(ResCode.INSERT_SUCCESS.code(), ResCode.INSERT_SUCCESS.msg());
               }
            }
        }
        return new ResMsg(ResCode.OPERATE_FAIL.code(), ResCode.OPERATE_FAIL.msg());
    }

    /**
     * 用户详情
     *
     * @param userId
     * @param map
     * @return
     */
    @RequestMapping("/userDetail")
    @AuthPassport
    public String userDetail(Long userId, ModelMap map) {
        if (userId != null) {
            User user = userService.findUserById(userId);
            map.addAttribute("user", user);
        }
        return "user/user_edit";
    }

    /**
     * 导出用户信息Excel
     *
     * @param condition
     * @return
     */
    @RequestMapping("/exportUserExcel")
    @AuthPassport
    public ModelAndView exportUserExcel(User condition) {
        condition.setDeleted("0");//未删除
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("list", userService.findUserExcelByCondition(condition));
        return new ModelAndView(new ViewExcel<UserModel>(), model);
    }

    @Autowired
    private ViewPDF viewPDF;

    /**
     * 导出用户信息PDF
     *
     * @param condition
     * @return
     */
    @RequestMapping("/exportUserPdf")
    @AuthPassport
    public ModelAndView exportUserPdf(User condition) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("list", userService.findUserExcelByCondition(condition));
        model.put("ftl", "pdf/users.ftl");
        return new ModelAndView(viewPDF, model);
    }

    /**
     * 用户导入
     *
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("/userImport")
    @ResponseBody
    @AuthPassport
    public ResMsg userImport(@RequestParam("file") MultipartFile file) throws Exception {
        // 判断类型和大小
        String suffix = StringUtil.getFileType(file.getOriginalFilename());
        if (!"xlsx".equals(suffix)) {
            return new ResMsg(ResCode.FAIL.code(), "仅支持xlsx文件格式");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return new ResMsg(ResCode.FAIL.code(), "文件最大10M");
        }

        ExcelReaderXSS read = new ExcelReaderXSS();
        Map<Integer, List> maps = read.readExcelContent(file.getInputStream());
        // return userService.generate(maps);
        return new ResMsg(ResCode.SUCCESS.code(), ResCode.SUCCESS.msg());
    }

    /**
     * 批量删除用户
     *
     * @param userIds
     * @return
     */
    @RequestMapping("/batchDeleteUser")
    @ResponseBody
    @AuthPassport(right = false)
    public ResMsg batchDeleteUser(String userIds){

        if (StringUtil.isBlank(userIds)) {
            return null;
        }
        String[] idStrs = userIds.split(",");
        Long[] ids = new Long[idStrs.length];
        for(int i=0;i<idStrs.length;i++){
            ids[i]= Long.valueOf(idStrs[i]);
        }

        int msg = userService.deleteBatch(ids);
        if(msg>0){
            return new ResMsg(0,ResCode.DELETE_SUCCESS.msg());
        }
        return new ResMsg(-1,ResCode.DELETE_FAIL.msg());
    }


    /**
     * 删除单个用户
     * @param userId
     * @return
     */
    @RequestMapping("/deleteUserByuserId")
    @ResponseBody
    @AuthPassport(right = false)
    public ResMsg deleteUserByuserId(String userId){

        if (StringUtil.isBlank(userId)) {
            return null;
        }

        int msg = userService.deleteUser(Long.valueOf(userId));
        if(msg>0){
            return new ResMsg(0,ResCode.DELETE_SUCCESS.msg());
        }
        return new ResMsg(-1,ResCode.DELETE_FAIL.msg());
    }



}
