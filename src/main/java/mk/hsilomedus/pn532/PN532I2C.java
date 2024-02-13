
package mk.hsilomedus.pn532;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.Pi4J;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.i2c.I2CRegister;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PN532I2C implements IPN532Interface {
	
    private static final int I2C_BUS = 1;
    private static final int I2C_DEVICE = 0x04;

	boolean debug = false;
	boolean debugReads = false;

	private byte command;
	private I2C i2c;
	private I2CRegister register;

	private static final int DEVICE_ADDRESS = 0x24;

	@Override
	public void begin() {
		
	    /*
		try {
			i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
			log.debug("Connected to bus OK!!!");

			i2cDevice = i2cBus.getDevice(DEVICE_ADDRESS);
			log.debug("Connected to device OK!!!");

			Thread.sleep(500);

		} catch (IOException e) {
			log.error("Exception: " + e.getMessage());
		} catch (InterruptedException e) {
			log.error("Interrupted Exception: " + e.getMessage());
		}
		*/

        // Initialize Pi4J with an auto context
        // An auto context includes AUTO-DETECT BINDINGS enabled
        // which will load all detected Pi4J extension libraries
        // (Platforms and Providers) in the class path
        var pi4j = Pi4J.newAutoContext();

        // create I2C config
        var config  = I2C.newConfigBuilder(pi4j)
                .id("my-i2c-bus")
                .name("My I2C Bus")
                .bus(I2C_BUS)
                .device(I2C_DEVICE)
                .build();

        // get a serial I/O provider from the Pi4J context
        I2CProvider i2CProvider = pi4j.provider("pigpio-i2c");

        // use try-with-resources to auto-close I2C when complete
        i2c = i2CProvider.create(config);

        // we will be reading and writing to register address 0x01
        register = i2c.register(0x01);

	}

	@Override
	public void wakeup() {

	}

	@Override
	public CommandStatus writeCommand(byte[] header, byte[] body) throws InterruptedException {
		log.debug("pn532i2c.writeCommand(header:" + getByteString(header) + ", body: " + getByteString(body) + ")");

		List<Byte> toSend = new ArrayList<Byte>();

		command = header[0];
		try {
			toSend.add(PN532_PREAMBLE);
			toSend.add(PN532_STARTCODE1);
			toSend.add(PN532_STARTCODE2);

			byte cmd_len = (byte) header.length;
			cmd_len += (byte) body.length;
			cmd_len++;
			byte cmdlen_1 = (byte) (~cmd_len + 1);

			toSend.add(cmd_len);
			toSend.add(cmdlen_1);

			toSend.add(PN532_HOSTTOPN532);

			byte sum = PN532_HOSTTOPN532;

			for (int i = 0; i < header.length; i++) {
				toSend.add(header[i]);
				sum += header[i];
			}

			for (int i = 0; i < body.length; i++) {
				toSend.add(body[i]);
				sum += body[i];
			}

			byte checksum = (byte) (~sum + 1);
			toSend.add(checksum);
			toSend.add(PN532_POSTAMBLE);
			byte[] bytesToSend = new byte[toSend.size()];
			for (int i = 0; i < bytesToSend.length; i++) {
				bytesToSend[i] = toSend.get(i);
			}
			log.debug("pn532i2c.writeCommand sending " + getByteString(bytesToSend));
			register.write(bytesToSend, 0, bytesToSend.length);

		} catch (Exception e) {
			log.error("pn532i2c.writeCommand exception occured: " + e.getMessage());
			return CommandStatus.INVALID_ACK;
		}
		log.debug("pn532i2c.writeCommand transferring to waitForAck())");
		return waitForAck(5000);

	}

	private CommandStatus waitForAck(int timeout) throws InterruptedException {
		log.debug("pn532i2c.waitForAck()");

		byte ackbuff[] = new byte[7];
		byte PN532_ACK[] = new byte[] { 0, 0, (byte) 0xFF, 0, (byte) 0xFF, 0 };

		int timer = 0;
		String message = "";
		while (true) {
			try {
				int read = register.read(ackbuff, 0, 7);
				if (debugReads && read > 0) {
					log.debug("pn532i2c.waitForAck Read " + read + " bytes.");
				}
			} catch (Exception e) {
				message = e.getMessage();
			}

			if ((ackbuff[0] & 1) > 0) {
				break;
			}

			if (timeout != 0) {
				timer += 10;
				if (timer > timeout) {
					log.debug("pn532i2c.waitForAck timeout occured: " + message);
					return CommandStatus.TIMEOUT;
				}
			}
			//Gpio.delay(10);

		}

		for (int i = 1; i < ackbuff.length; i++) {
			if (ackbuff[i] != PN532_ACK[i - 1]) {
				log.debug("pn532i2c.waitForAck Invalid Ack.");
				return CommandStatus.INVALID_ACK;
			}
		}
		log.debug("pn532i2c.waitForAck OK");
		return CommandStatus.OK;

	}

	@Override
	public CommandStatus writeCommand(byte[] header) throws InterruptedException {
		return writeCommand(header, new byte[0]);
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException {
		log.debug("pn532i2c.readResponse");

		byte response[] = new byte[expectedLength + 2];

		int timer = 0;

		while (true) {
			try {
				int read = register.read(response, 0, expectedLength + 2);
				if (debugReads && read > 0) {
					log.debug("pn532i2c.waitForAck Read " + read + " bytes.");
				}
			} catch (Exception e) {
				// Nothing, timeout will occur if an error has happened.
			}

			if ((response[0] & 1) > 0) {
				break;
			}

			if (timeout != 0) {
				timer += 10;
				if (timer > timeout) {
					log.debug("pn532i2c.readResponse timeout occured.");
					return -1;
				}
			}
			//Gpio.delay(10);

		}

		int ind = 1;

		if (PN532_PREAMBLE != response[ind++] || PN532_STARTCODE1 != response[ind++]
				|| PN532_STARTCODE2 != response[ind++]) {
			log.debug("pn532i2c.readResponse bad starting bytes found");
			return -1;
		}

		byte length = response[ind++];
		byte com_length = length;
		com_length += response[ind++];
		if (com_length != 0) {
			log.debug("pn532i2c.readResponse bad length checksum");
			return -1;
		}

		byte cmd = 1;
		cmd += command;

		if (PN532_PN532TOHOST != response[ind++] || (cmd) != response[ind++]) {
			log.debug("pn532i2c.readResponse bad command check.");
			return -1;
		}

		length -= 2;
		if (length > expectedLength) {
			log.debug("pn532i2c.readResponse not enough space");
			return -1;
		}

		byte sum = PN532_PN532TOHOST;
		sum += cmd;

		for (int i = 0; i < length; i++) {
			buffer[i] = response[ind++];
			sum += buffer[i];
		}

		byte checksum = response[ind++];
		checksum += sum;
		if (0 != checksum) {
			log.debug("pn532i2c.readResponse bad checksum");
			return -1;
		}

		return length;

	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength) throws InterruptedException {
		return readResponse(buffer, expectedLength, 1000);
	}

	private String getByteString(byte[] arr) {
		String output = "[";

		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				output += Integer.toHexString(arr[i]) + " ";
			}
		}
		return output.trim() + "]";
	}

	@Override
	public void sendAck() throws IllegalStateException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOffsetBytes() {
		// TODO Auto-generated method stub
		return 0;
	}
}
