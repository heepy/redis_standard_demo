package redis_case;

import dao.UserInfoDao;
import entity.UserInfo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import util.DataUtil;

import java.util.UUID;

/**
 * @author horan
 * create by horan on 2020/02/20
 * 数据库和缓存的双写问题，解决策略先更新数据库，再删除缓存，此方法在数据库读写分离时不适用，需要延迟双删策略
 */
public class DoubleWrite {
    private final JedisPool jedisPool;
    private final UserInfoDao userInfoDao;
    public DoubleWrite(JedisPool jedisPool,UserInfoDao userInfoDao) {
        this.jedisPool =jedisPool ;
        this.userInfoDao = userInfoDao;
    }
    //更新数据
    public void updateData(UserInfo userInfo){
        Jedis conn = null;

        try {
            // 获取连接
            conn = jedisPool.getResource();
            //先更新数据库
            try {
                userInfoDao.updateUserInfo(userInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //使得缓存中的数据失效
            String oldKey=String.valueOf(userInfo.getId());
            String value=conn.get(oldKey);
            if(value!=null){
              conn.del(oldKey);
            }

            // 锁名，即key值
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

    }
    //获取数据
    public String getData(String key){ //此处应该符合原子性，使用APT SET
        Jedis conn = null;
        String value=null;
        try {
            // 获取连接
            conn = jedisPool.getResource();
            // 获取Key

            value=conn.get(key);
            //如果命中
            if(value!=null){
                return value;
            }else{ //否则更新缓存
                int id=Integer.parseInt(key);
                UserInfo userInfo=userInfoDao.getUserInfo(id);
                String newValue=userInfo.getUserName();
                conn.setex(key,120,newValue); //设置过期时间为120秒
                return newValue;
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }

    /**
     * 此方法可以解决缓存穿透问题，若数据库中没有数据，为防止缓存穿透造成数据库压力，可以返回一个空值
     * @author horan
     * @param key
     */
    public String getDataIsEmpty(String key){
        Jedis conn = null;
        String value=null;
        try {
            // 获取连接
            conn = jedisPool.getResource();
            // 获取Key

            value=conn.get(key);
            //如果命中
            if(value!=null){
                return value;
            }else{ //否则更新缓存
                int id=Integer.parseInt(key);
                UserInfo userInfo=userInfoDao.getUserInfo(id);
                String newValue=null;
                if(userInfo!=null){
                    newValue=userInfo.getUserName();
                    conn.setex(key,120,newValue);
                }
                newValue="没有数据";
                conn.setex(key,120,newValue);       //设置过期时间为120秒
                return newValue;
            }
        } catch (JedisException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }
}
