import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class CacheTest {

    private LocalCache localCache = LocalCache.getInstance();

    /***
     * set、get、remove、contains基本测试
     */
    @Test
    public void test1(){
        for (int i = 0; i < 10; i++) {
            localCache.set(String.valueOf(i),"value" + i);
        }

        localCache.remove("5");

        for (int i = 0; i < 10; i++) {
            if (localCache.contains(String.valueOf(i))){
                System.out.println(localCache.get(String.valueOf(i)));
            }
        }
    }

    /***
     * 自动清理过期对象
     */
    @Test
    public void test2(){
        localCache.set("key1","value1",1000);//设置存留时间为1s
        localCache.set("key2","value2",-1);//时间为负值永久保留
        System.out.println(localCache.get("key1"));
        System.out.println(localCache.get("key2"));
        try {
            Thread.sleep(6000);    //默认清理时间间隔为5s
            System.out.println("key1 contain?" + localCache.contains("key1"));
            System.out.println("key2 contain?" + localCache.contains("key2"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对象持久化，本地文件存储
     */
    @Test
    public void test3(){
        for (int i = 0; i < 10; i++) {
            localCache.set(String.valueOf(i),"value" + i);
        }
        localCache.save("test");//存储到文件名为test的文件

        HashMap<String,Object> map = localCache.load("test");
        for (int i = 0; i < 10; i++) {
            System.out.println(localCache.get(String.valueOf(i)));
        }
    }

    /**
     * LRU算法回收测试
     */
    @Test
    public void test4(){
        for (int i = 0; i < 10; i++) {
            localCache.set(String.valueOf(i),"value" + i);
        }

        localCache.get("0");//调用，使其不会置于回收队列头
        localCache.get("1");

        localCache.set("10",10);
        localCache.set("11",11);

        for (int i = 0; i < 12; i++) {
            System.out.println(i + "contains? " +localCache.contains(String.valueOf(i)));
        }
    }

    /**
     * 读写拷贝方法clone()测试
     */
    @Test
    public void test5(){
        try {
            String s = "123";

            Class<?> localCacheC = Class.forName("LocalCache");
            Method getEntry = localCacheC.getDeclaredMethod("createEntry", Object.class, long.class);
            getEntry.setAccessible(true);
            Method clone = localCacheC.getDeclaredMethod("clone", CacheEntry.class);
            clone.setAccessible(true);
            CacheEntry cacheEntry = (CacheEntry) getEntry.invoke(localCache,s,1000);
            CacheEntry cacheEntry1 = (CacheEntry) clone.invoke(localCache,cacheEntry);
            System.out.println(cacheEntry == cacheEntry1);//深度赋值
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /***
     *多线程测试
     */
//    @Test
//    public void test6(){
//
//    }

//    public static void main(String[] args) {
//        LocalCache localCache = LocalCache.getInstance();
//        for (int i = 0; i < 10; i++) {
//            final String key = "key" + i;
//            final String value = "value" + i;
//            new Thread(() -> {
//                localCache.set(key,value);
//            }).start();
//        };
//
//        for (int i = 0; i < 10; i++) {
//            final String key = "key" + i;
//            new Thread(() -> {
//                System.out.println(localCache.get(key));
//            }).start();
//        }
//    }
}
