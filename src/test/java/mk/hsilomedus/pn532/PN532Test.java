package mk.hsilomedus.pn532;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PN532Test {
	
	static final byte PN532_MIFARE_ISO14443A = 0x00;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		try {
//		IPN532Interface pn532Interface = new PN532Spi();
	  IPN532Interface pn532Interface = new PN532I2C();
		PN532 nfc = new PN532(pn532Interface);

		// Start
		System.out.println("Starting up...");
		nfc.begin();
		Thread.sleep(1000);

		long versiondata = nfc.getFirmwareVersion();
		if (versiondata == 0) {
			System.out.println("Didn't find PN53x board");
			return;
		}
		// Got ok data, print it out!
		System.out.print("Found chip PN5");
		System.out.println(Long.toHexString((versiondata >> 24) & 0xFF));

		System.out.print("Firmware ver. ");
		System.out.print(Long.toHexString((versiondata >> 16) & 0xFF));
		System.out.print('.');
		System.out.println(Long.toHexString((versiondata >> 8) & 0xFF));

		// configure board to read RFID tags
		nfc.SAMConfig();

		System.out.println("Waiting for an ISO14443A Card ...");

		byte[] buffer = new byte[8];
		while (true) {
			buffer = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A);
			
			int readLength = buffer.length;

			if (readLength > 0) {
				System.out.println("Found an ISO14443A card");

				System.out.print("  UID Length: ");
				System.out.print(readLength);
				System.out.println(" bytes");

				System.out.print("  UID Value: [");
				for (int i = 0; i < readLength; i++) {
					System.out.print(Integer.toHexString(buffer[i]));
				}
				System.out.println("]");
			}

			Thread.sleep(100);
		}
		} catch (Exception e) {
			
		}
	}

}
