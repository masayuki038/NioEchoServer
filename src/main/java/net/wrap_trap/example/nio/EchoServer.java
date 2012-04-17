package net.wrap_trap.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EchoServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<ByteBuffer>();

    public EchoServer() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().setReuseAddress(true);
        serverChannel.socket().bind(new InetSocketAddress(8000));

        serverChannel.configureBlocking(false);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        System.out.println("start");
        try {
            while (selector.select() > 0) {
                System.out.println("select loop");
                Set<SelectionKey> keys = selector.selectedKeys();
                for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isAcceptable()) {
                        System.out.println("accept");
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(key.selector(), (SelectionKey.OP_READ | SelectionKey.OP_WRITE));
                    } else {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (key.isReadable()) {
                            System.out.println("read");
                            ByteBuffer buffer = ByteBuffer.allocate(4096);
                            if (channel.read(buffer) >= 0) {
                                buffer.flip();
                                queue.add(buffer);
                            }
                        } else if (key.isWritable()) {
                            System.out.println("write");
                            if (!queue.isEmpty()) {
                                ByteBuffer buffer = queue.poll();
                                channel.write(buffer);
                            }
                        }
                    }
                }
            }
        } finally {
            for (SelectionKey key : selector.keys()) {
                try {
                    key.channel().close();
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        EchoServer echoServer = new EchoServer();
        echoServer.start();
    }
}
