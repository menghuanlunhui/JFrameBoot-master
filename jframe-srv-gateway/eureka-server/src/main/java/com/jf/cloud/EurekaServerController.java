package com.jf.cloud;

import com.jf.cloud.common.Result;
import com.jf.cloud.common.StringUtil;
import com.jf.cloud.config.OAuthClient;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.cluster.PeerEurekaNode;
import com.netflix.eureka.util.StatusInfo;
import com.netflix.eureka.util.StatusUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: admin
 * Date: 2018-02-01
 * Time: 15:15
 */
@Controller
public class EurekaServerController {

    @Resource
    private ApplicationInfoManager manager;
    @Resource
    private OAuthClient oAuthClient;

    /**
     * 登录
     *
     * @param error
     * @param logout
     * @param map
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout, ModelMap map) {
        if (error != null) {
            map.addAttribute("msg", "不正确的用户名和密码");
        }
        if (logout != null) {
            map.addAttribute("msg", "你已经成功退出");
        }
        return "login";
    }

    /*@RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("logout");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("logout success");
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }*/

    /**
     * 首页
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/", method = {RequestMethod.GET})
    public String index(ModelMap map) {
        EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();
        EurekaInstanceConfig config = manager.getEurekaInstanceConfig();
        // eureka信息
        map.addAttribute("info", config);
        InstanceInfo instanceInfo = manager.getInfo();
        // eureka实例信息
        map.addAttribute("instanceInfo", instanceInfo);
        StatusInfo statusInfo = new StatusUtil(context).getStatusInfo();
        // 状态信息
        map.addAttribute("statusInfo", statusInfo);
        List<PeerEurekaNode> nodes = context.getPeerEurekaNodes().getPeerNodesView();
        // 节点信息
        map.addAttribute("nodes", nodes);

        map.addAttribute("currentTime", StatusInfo.getCurrentTimeAsString());
        map.addAttribute("upTime", StatusInfo.getUpTime());
        map.addAttribute("environment", ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
        map.addAttribute("datacenter", ConfigurationManager.getDeploymentContext().getDeploymentDatacenter());
        return "status";
    }

    /**
     * 已注册实例
     *
     * @param map
     * @return
     */
    @RequestMapping("/clients")
    public String clients(ModelMap map) {
        EurekaServerContext context = EurekaServerContextHolder.getInstance().getServerContext();
        List<Application> applications = context.getRegistry().getSortedApplications();
        map.addAttribute("applications", applications);
        return "clients";
    }

    /**
     * 实例信息
     *
     * @param instance 实例ip http://ip:port/monitor
     * @param map
     * @return
     */
    @RequestMapping("/infos")
    public String infos(String instance, ModelMap map) {
        if (StringUtil.isBlank(instance)) return "";
        return "info";
    }

    /**
     * 通过http(Auth) 获取实例的健康状态
     *
     * @param monitor http://ip:port/monitor/health
     * @return json
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public Result info(String monitor) throws Exception {
        if (StringUtil.isBlank(monitor)) {
            return new Result(1, "invalid param");
        }
        String result = oAuthClient.requestAuth(monitor, HttpMethod.GET);
        return new Result(0, "SUCCESS", result);
    }

    /**
     * 配置刷新
     *
     * @param monitor http://ip:port/monitor/health
     * @return
     */
    @RequestMapping(value = "/info/refresh", method = RequestMethod.POST)
    @ResponseBody
    public Result refresh(String monitor) throws Exception {
        if (StringUtil.isBlank(monitor)) {
            return new Result(1, "invalid param");
        }
        String result = oAuthClient.requestAuth(monitor, HttpMethod.POST);
        return new Result(0, "SUCCESS", result);
    }


    /**
     * 配置
     *
     * @return
     */
    @RequestMapping("/configs")
    public String configs() {
        return "configs";
    }

}
