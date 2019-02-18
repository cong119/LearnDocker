import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoNIOServer {

    private final static int PORT = 8088;
    private final static int BUF_SIZE = 10240;

    private void initServer() {

        try {
            //创建通道管理器对象selector
            Selector selector = Selector.open();

            //创建一个通道对象Channel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));

            //将上述通道对象和通道管理器绑定，并为该通道注册OP_ACCEPT事件
            //注册时间后，当该事件到达时，selector.select()会返回，若事件未到达select()方法阻塞
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keySet.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        doAccept(key);
                    } else if (key.isReadable()) {
                        doRead(key);
                    } else if (key.isWritable() && key.isValid()) {
                        doWrite(key);
                    } else if (key.isConnectable()) {
                        System.out.println("连接成功!!!");
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void doAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            System.out.println("ServerSocketChannel正在循环监听");

            SocketChannel clientChannel = serverSocketChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doRead(SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUF_SIZE);

            long readBytes = clientChannel.read(byteBuffer);
            while (readBytes > 0) {
                byteBuffer.flip();
                byte[] data = byteBuffer.array();
                String info = new String(data).trim();

                System.out.println("从客户端发送过来的消息是：" + info);
                byteBuffer.clear();
                readBytes = clientChannel.read(byteBuffer);
            }

            String info = "客户端你好!!!";

            byteBuffer.clear();
            byteBuffer.put(info.getBytes("UTF-8"));
            byteBuffer.flip();

            clientChannel.write(byteBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doWrite(SelectionKey key) {

        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUF_SIZE);
            byteBuffer.flip();
            SocketChannel clientChannel = (SocketChannel) key.channel();

            while(byteBuffer.hasRemaining()) {
                clientChannel.write(byteBuffer);
            }

            byteBuffer.compact();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EchoNIOServer echoNIOServer = new EchoNIOServer();
        echoNIOServer.initServer();
    }
}
