package Distributedlock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 改进程序，中间的过程参考笔记文档
 */
@RestController
public class Controller2 {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @RequestMapping("/deduct2")
    public String deduckStock() throws InterruptedException{
        //针对每一个进来的线程加锁，释放的时候要持有锁才能解锁，才不会释放掉其他还在执行线程的锁。
        String clientId = UUID.randomUUID().toString();
        String lockey = "product001";

        //默认连接的是本地的6379的端口，所以在yml文件中配置ip
        try {
            //原子操作，赋值，时间，即使中间服务器问题最后没有释放锁，还是可以释放
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockey, clientId, 15, TimeUnit.SECONDS);
            //如果有线程进来，保持线程安全，返回
            if (!result) { //已经设置过值了，
                return "error"; //可以进行等待的具体逻辑
            }
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock",realStock+"");
                System.out.println("扣减成功，剩余库存"+realStock);
            }else {
                System.out.println("扣减失败，剩余库存不足");
            }
        } finally {
            //假如上面的程序有异常，锁得不到释放一直卡住，所以最后释放锁
            if (lockey.equals(stringRedisTemplate.opsForValue().get(lockey))) {
                stringRedisTemplate.delete(lockey);
            }
        }
        return "end";
    }
/**
 * 还有问题，假如设置的过期时间太短，第一个程序还没有执行完（正在处理业务逻辑）,
 * 释放了锁，另一个线程进来了，此时两个线程在处理同一个资源，就是没挡住，
 * 还是可能有安全隐患。
 *
 *  解决办法：在执行业务逻辑的时候，每隔设置时间的1/3去查询一下是否过期，
 *  没有就加上原来设置的时间。最后主线程执行完，从线程也执行完了，
 *  自己处理可能一堆坑，有分布式锁框架Redisson
 */
}
