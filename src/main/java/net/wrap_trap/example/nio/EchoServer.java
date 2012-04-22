package net.wrap_trap.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EchoServer {

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

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

                    if (key.isAcceptable()) {
                        System.out.println("accept");
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(key.selector(), SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        System.out.println("read");
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        if (channel.read(buffer) > 0) {
                            buffer.flip();
                            buffers.add(buffer);
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    } else if (key.isWritable()) {
                        System.out.println("write");
                        SocketChannel channel = (SocketChannel) key.channel();
                        for (ByteBuffer buffer : buffers) {
                            channel.write(buffer);
                        }
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    it.remove();
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
