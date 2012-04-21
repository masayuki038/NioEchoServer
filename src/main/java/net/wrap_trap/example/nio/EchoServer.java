package net.wrap_trap.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;

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
                        channel.register(key.selector(), SelectionKey.OP_READ);
                    } else {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (key.isReadable()) {
                            System.out.println("read");
                            ByteBuffer buffer = ByteBuffer.allocate(4096);
                            channel.read(buffer);
                            buffer.flip();
                            channel.write(buffer);
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
