import java.io.*;
import java.net.*;

public class MulticastServer {
    public static void main(String[] args) throws Exception {
        // Create a multicast socket and join the multicast group
        InetAddress group = InetAddress.getByName("230.0.0.1");
        MulticastSocket multicastSocket = new MulticastSocket();
        multicastSocket.joinGroup(group);

        // Read the file from disk
        File file = new File("Sylas1.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);

        // Define the packet size and buffer
        int packetSize = 1024;
        byte[] buffer = new byte[packetSize];

        // Define the sequence number and total number of packets
        int sequenceNumber = 0;
        int totalPackets = (int) Math.ceil((double) file.length() / packetSize);

        // Read the file and send packets to the multicast group
        while (true) {
            // Read a packet from the file input stream
            int bytesRead = fileInputStream.read(buffer);

            // Check if we have reached the end of the file
            if (bytesRead == -1) {
                break;
            }

            // Construct the packet with sequence number and total packets
            byte[] sequenceBytes = intToBytes(sequenceNumber);
            byte[] totalPacketsBytes = intToBytes(totalPackets);
            byte[] data = new byte[bytesRead + 8];
            System.arraycopy(sequenceBytes, 0, data, 0, 4);
            System.arraycopy(totalPacketsBytes, 0, data, 4, 4);
            System.arraycopy(buffer, 0, data, 8, bytesRead);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, 4446);

            // Send the packet to the multicast group
            multicastSocket.send(packet);

            // Increment the sequence number
            sequenceNumber++;
        }

        // Send an "END" packet to signal the end of the file transfer
        byte[] endPacketData = "END".getBytes();
        DatagramPacket endPacket = new DatagramPacket(endPacketData, endPacketData.length, group, 4446);
        multicastSocket.send(endPacket);

        // Close the file input stream and the multicast socket
        fileInputStream.close();
        multicastSocket.close();
    }

    private static byte[] intToBytes(int n) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (n & 0xFF);
        bytes[1] = (byte) ((n >> 8) & 0xFF);
        bytes[2] = (byte) ((n >> 16) & 0xFF);
        bytes[3] = (byte) ((n >> 24) & 0xFF);
        return bytes;
    }
}
