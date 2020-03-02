package redis_case;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collections;

/**
 * 分布式锁的简单实现代码
 * @author horan on 2020/02/17
 * 分布式锁的具备的几个条件
 * 1、在分布式系统环境下，一个方法在同一时间只能被一个机器的一个线程执行；
 * 2、高可用的获取锁与释放锁；
 * 3、高性能的获取锁与释放锁；
 * 4、具备可重入特性；
 * 5、具备锁失效机制，防止死锁；
 * 6、具备非阻塞锁特性，即没有获取到锁将直接返回获取锁失败
 */
public class DistributedLock {


    private final JedisPool jedisPool;
    private String lock_key="lock";
    private  final Long RELEASE_SUCCESS = 1L;

    public DistributedLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 加锁
     * @param identifier  锁的唯一标识
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间
     * @return 锁标识
     */
    public boolean lockWithTimeout(String identifier, long acquireTimeout, long timeout) {
        Jedis conn = null;
        try {
            // 获取连接
            conn = jedisPool.getResource();


            // 获取锁的超时时间，超过这个时间则放弃获取锁
            long start=System.currentTimeMillis();

            while (true) {
               String result= conn.set(lock_key,identifier,"NX","EX",acquireTimeout); //保证原子性，不能使用setNx
                if("OK".equals(result)){
                    return  true;
                }
                //否则循环等待，在timeout时间内仍未获取到锁，则获取失败
                long l = System.currentTimeMillis()-start;
                if (l>=timeout) {
                    System.out.println("线程"+identifier+"---------超时："+l);
                    return false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return true;
    }

    /**
     * 释放锁
     * @param identifier 释放锁的标识
     * @return
     */
    public boolean releaseLock(String identifier) {
        Jedis conn = null;


        try {
            conn = jedisPool.getResource();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Object result = conn.eval(script, Collections.singletonList(lock_key), //保证原子性
                        Collections.singletonList(identifier));
//            Long result=null;
//            if(identifier.equals(conn.get(lock_key))){
//              result= conn.del(lock_key);
//            }
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;

        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return false;
    }
}