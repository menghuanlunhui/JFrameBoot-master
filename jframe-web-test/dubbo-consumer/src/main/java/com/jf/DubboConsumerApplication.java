package com.jf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: admin
 * Date: 2019-03-19
 * Time: 11:55
 */
@SpringBootApplication(scanBasePackages = "com.jf")
public class DubboConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboConsumerApplication.class, args);
    }

}
