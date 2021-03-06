package com.github.hackerwin7.mysql.binlog.tracker.mysql.driver;

import com.github.hackerwin7.mysql.binlog.tracker.mysql.driver.packets.server.ErrorPacket;
import com.github.hackerwin7.mysql.binlog.tracker.mysql.driver.packets.client.QueryCommandPacket;
import com.github.hackerwin7.mysql.binlog.tracker.mysql.driver.packets.server.OKPacket;
import com.github.hackerwin7.mysql.binlog.tracker.mysql.driver.utils.PacketManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 默认输出的数据编码为UTF-8，如有需要请正确转码
 * 
 * @author jianghang 2013-9-4 上午11:51:11
 * @since 1.0.0
 */
public class MysqlUpdateExecutor {

    private static final Logger logger = Logger.getLogger(MysqlUpdateExecutor.class);

    private SocketChannel channel;

    public MysqlUpdateExecutor(MysqlConnector connector){
        if (!connector.isConnected()) {
            throw new RuntimeException("should execute connector.connect() first");
        }

        this.channel = connector.getChannel();
    }

    public MysqlUpdateExecutor(SocketChannel ch){
        this.channel = ch;
    }

    public OKPacket update(String updateString) throws IOException {
        QueryCommandPacket cmd = new QueryCommandPacket();
        cmd.setQueryString(updateString);
        byte[] bodyBytes = cmd.toBytes();
        PacketManager.write(channel, bodyBytes);

        logger.debug("read update result...");
        byte[] body = PacketManager.readBytes(channel, PacketManager.readHeader(channel, 4).getPacketBodyLength());
        if (body[0] < 0) {
            ErrorPacket packet = new ErrorPacket();
            packet.fromBytes(body);
            throw new IOException(packet + "\n with command: " + updateString);
        }

        OKPacket packet = new OKPacket();
        packet.fromBytes(body);
        return packet;
    }
}
