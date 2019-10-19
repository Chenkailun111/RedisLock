package Distributedlock.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class Controller3 {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Redisson redisson;

    @RequestMapping("/deduct3")
    public String deduckStock() throws InterruptedException{
        //针对每一个进来的线程加锁，释放的时候要持有锁才能解锁，才不会释放掉其他还在执行线程的锁。
        String clientId = UUID.randomUUID().toString();
        String lockey = "product001";
        //获取锁
        RLock lock = redisson.getLock(lockey);

        //默认连接的是本地的6379的端口，所以在yml文件中配置ip
        try {
            //原子操作，赋值，时间，即使中间服务器问题最后没有释放锁，还是可以释放
       /* Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockey, clientId, 15, TimeUnit.SECONDS);
        //如果有线程进来，保持线程安全，返回
        if (!result) { //已经设置过值了，
            return "error"; //可以进行等待的具体逻辑
        }*/

       //现在处理方式，底层默认实现业务逻辑中处理查询，业务中锁过期的时候，自动加上设置的时长。
           lock.tryLock(30,TimeUnit.SECONDS);

            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock",realStock+"");
                System.out.println("扣减成功，剩余库存"+realStock);
            }else {
                System.out.println("扣减失败，剩余库存不足");
            }
        } finally {
            /*if (lockey.equals(stringRedisTemplate.opsForValue().get(lockey))) {
                stringRedisTemplate.delete(lockey);
            }*/
            lock.unlock();
        }
        return "end";
    }
}
