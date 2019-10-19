package Distributedlock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 注意获取不到连接池，可能是springboot版本不对
 *
 * 秒杀自我解析，代码一步步演进,出现的问题，
 * 如果高并发情况下，都获取到50，但是最后进入都是50-1，结果最后只-1
 */
@RestController
public class Controller1 {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/")
    public String test(){
        System.out.println("test ----------");
//        System.out.println(new Jedis("192.168.43.100",6379).ping());
        return "for me";
    }
    @RequestMapping("/deduct1")
    public String deduckStock() throws InterruptedException{


        //默认连接的是本地的6379的端口
        int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
        if (stock > 0) {
            int realStock = stock - 1;
            stringRedisTemplate.opsForValue().set("stock",realStock+"");
            System.out.println("扣减成功，剩余库存"+realStock);
        }else {
            System.out.println("扣减失败，剩余库存不足");
        }
        return "end";
    }
}
