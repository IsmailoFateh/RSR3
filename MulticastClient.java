import java.io.*;
import java.net.*;

public class MulticastClient {
    public static void main(String[] args) throws Exception {
        // Create a multicast socket and join the multicast group
        InetAddress group = InetAddress.getByName("230.0.0.1");
        MulticastSocket multicastSocket = new MulticastSocket(4446);
        multicastSocket.joinGroup(group);

        // Create a file output stream to write the received file to disk
        FileOutputStream fileOutputStream = new FileOutputStream("received.jpg");

        // Define the packet size and buffer
        int packetSize = 1024;
        byte[] buffer = new byte[packetSize];

        // Define the expected sequence number and total number of packets
        int expectedSequenceNumber = 0;
        int totalPackets = -1;

        // Receive packets from the multicast group and write them to disk
        while (true) {
            // Receive a packet from the multicast group
            DatagramPacket packet = new DatagramPacket(buffer, packetSize);
            multicastSocket.receive(packet);

            // Extract the sequence number and total packets from the packet data
            byte[] data = packet.getData();
            int sequenceNumber = bytesToInt(data, 0);
            int packetTotal = bytesToInt(data, 4);

            // Check if this is the "END" packet
            if (new String(data, 8, 3).equals("END")) {
                break;
            }

            // Check if this is the expected packet
            if (sequenceNumber == expectedSequenceNumber) {
                // Write the packet data to the file output stream
                fileOutputStream.write(data, 8, packet.getLength() - 8);

                // Increment the expected sequence number
                expectedSequenceNumber++;

                // Set the total packets if we haven't already
                if (totalPackets == -1) {
                    totalPackets = packetTotal;
                }

                // Check if we have received all the packets
                if (expectedSequenceNumber == totalPackets) {
                    break;
                }
            } else {
                // If this is not the expected packet, discard it
                System.out.println("Discarding packet " + sequenceNumber);
            }
        }

        // Close the file output stream and the multicast socket
        fileOutputStream.close();
        multicastSocket.close();
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset + 3] & 0xFF) << 24) |
               ((bytes[offset + 2] & 0xFF) << 16) |
               ((bytes[offset + 1] & 0xFF) << 8) |
               (bytes[offset] & 0xFF);
    }
}
