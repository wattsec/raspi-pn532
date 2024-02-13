package mk.hsilomedus.pn532;

import java.io.IOException;

// Author: ScottyDoesKnow
// Built for I2C and UART 
public class Pn532Thread extends Thread {

	

	public interface Pn532ThreadListener {
		void println(String message);

		void uidReceived(String channel, String uid);
	}

	private Pn532ThreadListener listener;
	private PN532Constant.Channel channel;

	public Pn532Thread(Pn532ThreadListener listener, PN532Constant.Channel channel) {
		this.listener = listener;
		this.channel = channel;
	}

	private String getChannelString() {
		return (channel == null)?null:channel.toString();
	}

	@Override
	public void run() {
		IPN532Interface pn532Interface;
		pn532Interface = switch (channel) {
		case UART: {
			yield new PN532SerialPi();
		}
		case I2C: {
			yield new PN532I2C();
		}
		case SERIAL: {
			yield new PN532SerialPort();
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + channel);
		};

		PN532 pn532 = new PN532(pn532Interface);

		try {
			pn532.begin();
		} catch (IOException | InterruptedException e) {
			listener.println("PN5xx " + getChannelString() + " begin error: " + e.getMessage());
			return;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			listener.println("PN5xx " + getChannelString() + " initialization Thread.sleep interrupted.");
			return;
		}

		long version;
		try {
			version = pn532.getFirmwareVersion();
		} catch (InterruptedException | IllegalStateException | IOException e) {
			listener.println("PN5xx " + getChannelString() + " getFirmwareVersion error: " + e.getMessage());
			return;
		}
		if (version == 0) {
			listener.println("Couldn't find PN5xx " + getChannelString() + ".");
			return;
		}
		listener.println("Found PN5" + Long.toHexString((version >> 24) & 0xFF) + " " + getChannelString() + " - FW: "
				+ Long.toHexString((version >> 16) & 0xFF) + "." + Long.toHexString((version >> 8) & 0xFF));

		// Configure board to read RFID tags
		try {
			pn532.SAMConfig();
		} catch (InterruptedException | IllegalStateException | IOException e) {
			listener.println("PN5xx " + getChannelString() + " SAMConfig error: " + e.getMessage());
			return;
		}
		listener.println("PN5xx " + getChannelString() + " running.");

		byte[] buffer = null;
		while (true) {
			try {
				buffer = pn532.readPassiveTargetID(PN532Constant.PN532_MIFARE_ISO14443A);
			} catch (InterruptedException | IllegalStateException | IOException e) {
				listener.println("PN5xx " + getChannelString() + " readPassiveTargetID error: " + e.getMessage());
				return;
			}

			if (buffer!= null && buffer.length > 0) {
				String uid = "";

				for (int i = 0; i < buffer.length; i++) {
					uid += Integer.toHexString(buffer[i]);
				}

				listener.uidReceived(getChannelString(), uid);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				listener.println("PN5xx " + getChannelString() + " running Thread.sleep interrupted.");
				return;
			}
		}
	}
}
