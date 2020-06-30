import java.io.*;

/***
 * 对象序列化工具类
 * @Author: Wall
 * @version 1.0 2020/06/30
 */
public class ObjectPersistenceUtils {

    public static void store(String name,Object object) throws IOException {
        String fileName = name;
        File file = new File("src/file/" + fileName);
        if (!file.exists()){
                file.createNewFile();
        }

        FileOutputStream outputStream = new FileOutputStream(file);
        store(outputStream,object);
    }

    public static void store(FileOutputStream outputStream,Object object) throws IOException {
        try (ObjectOutputStream oos= new ObjectOutputStream(outputStream)){
            oos.writeObject(object);
            oos.flush();
        }
    }

    public static Object load(String name) throws IOException, ClassNotFoundException {
        String fileName = name;
        File file = new File(fileName);
        if (!file.exists()){
            return null;
        }

        FileInputStream inputStream = new FileInputStream(file);
        return load(inputStream);
    }

    public static Object load(FileInputStream inputStream) throws IOException, ClassNotFoundException {
        Object obj = null;
        try (ObjectInputStream ois= new ObjectInputStream(inputStream)){
            obj = ois.readObject();
        }

        return obj;
    }
}
