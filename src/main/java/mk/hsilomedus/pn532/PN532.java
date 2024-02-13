package mk.hsilomedus.pn532;

import java.io.IOException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mk.hsilomedus.pn532.PN532Constant.KeyValue;
import mk.hsilomedus.pn532.PN532Constant.Tag;

@Slf4j
public class PN532 {
	
	private IPN532Interface medium;
	private byte[] pn532_packetbuffer;
	@Setter
	boolean debug = true;
	@Setter
	boolean trace = false;
	

	public PN532(IPN532Interface medium) {
		this.medium = medium;
		this.pn532_packetbuffer = new byte[64];
	}

	public void begin() throws IOException, InterruptedException {
		try {
			medium.begin();
			medium.wakeup();
		} catch (UnsupportedBoardType | UnsupportedBusNumberException e) {
			throw new RuntimeException("Error beginning: " + e.getMessage());
		}
	}

	public long getFirmwareVersion() throws InterruptedException, IllegalStateException, IOException {
		long response;

		byte[] command = new byte[1];
		command[0] = PN532Constant.PN532_COMMAND_GETFIRMWAREVERSION;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return 0;
		}

		int offset = 0; // medium.getOffsetBytes();

		response = pn532_packetbuffer[offset + 0];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 1];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 2];
		response <<= 8;
		response |= pn532_packetbuffer[offset + 3];

		return response;
	}

	public int SAMConfig() throws InterruptedException, IllegalStateException, IOException {
		
		log.debug("Set SAMConfig");
		
		byte[] command = new byte[4];
		command[0] = PN532Constant.PN532_COMMAND_SAMCONFIGURATION;
		command[1] = 0x01; // normal mode;
		command[2] = 0x00; // Only in virtual mode (0x02)
		//command[3] = 0x01; // use IRQ pin!
		
		// Send ack to cancel any previous command
		//medium.sendAck();

		var result = medium.writeCommand(command);
		if (result != CommandStatus.OK) {
			log.warn("write command not ok : {}", result);
			return 0;
		}

		return medium.readResponse(pn532_packetbuffer, 8);
	}

	public byte[] readPassiveTargetID(byte cardbaudrate) throws InterruptedException, IllegalStateException, IOException {
		
		if (debug) log.debug("readPassiveTargetID()");
		
		byte[] command = new byte[3];
		command[0] = PN532Constant.PN532_COMMAND_INLISTPASSIVETARGET;
		command[1] = 1; // max 1 cards at once (we can set this to 2 later)
		command[2] = (byte) cardbaudrate;
		
		// Send ack to cancel any previous command
		//medium.sendAck();

		var result = medium.writeCommand(command);
		if (result != CommandStatus.OK) {
			log.warn("Error writing: {}", result);
			return null; // command failed
		}

		// read data packet
		// if (medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length) < 0) {
		int aux = medium.readResponse(pn532_packetbuffer, 20);
		if (aux < 0) {
			log.warn("Problem read response: {}", aux);
			return null;
		}

		// check some basic stuff
	    /* ISO14443A card response should be in the following format:
	      byte            Description
	      -------------   ------------------------------------------
	      b0              Tags Found
	      b1              Tag Number (only one used in this example)
	      b2..3           SENS_RES
	      b4              SEL_RES
	      b5              NFCID Length
	      b6..NFCIDLen    NFCID
	    */

		int offset = medium.getOffsetBytes();

		if (pn532_packetbuffer[offset + 0] != 1) {
			log.warn("Problem byte 0 : {} != 1", pn532_packetbuffer[offset + 0]);
			return null;
		}
		// int sens_res = pn532_packetbuffer[2];
		// sens_res <<= 8;
		// sens_res |= pn532_packetbuffer[3];

		// DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
		// DMSG("SAK: 0x"); DMSG_HEX(pn532_packetbuffer[4]);
		// DMSG("\n");

		/* Card appears to be Mifare Classic */
		int uidLength = pn532_packetbuffer[offset + 5];
		byte[] buffer = new byte[uidLength];

		for (int i = 0; i < uidLength; i++) {
			buffer[i] = pn532_packetbuffer[offset + 6 + i];
		}

		return buffer;
	}
	
	public byte[] autopoll(byte type) throws InterruptedException, IllegalStateException, IOException {
		
		if (debug) log.debug("autopoll()");
		
		byte[] command = new byte[8];
		command[0] = PN532Constant.PN532_COMMAND_INAUTOPOLL;
		command[1] = 0x01; // PollNr 0x01 – 0xFE : 1 up to 254 polling / 0xFF : Endless polling.
		command[2] = 0x01; // (0x01 – 0x0F) indicates the polling period in units of 150 ms
		command[3] = 0x00; //Type 1 indicates the mandatory target type to be polled at the 1st time,
		command[4] = 0x03; //Type 1 indicates the mandatory target type to be polled at the 1st time,
		command[5] = 0x10; //Type 1 indicates the mandatory target type to be polled at the 1st time,
		command[6] = 0x20; //Type 1 indicates the mandatory target type to be polled at the 1st time,
		command[7] = 0x23; //Type 1 indicates the mandatory target type to be polled at the 1st time,
		// Type2-N Optional
		
		// Send ack to cancel any previous command
		//medium.sendAck();

		var result = medium.writeCommand(command);
		if (result != CommandStatus.OK) {
			log.warn("Error writing: {}", result);
			return null; // command failed
		}

		// read data packet
		// if (medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length) < 0) {
		int aux = medium.readResponse(pn532_packetbuffer, 20);
		if (aux < 0) {
			log.warn("Problem read response: {}", aux);
			return null;
		}

		/*
		 * ISO14443A card response should be in the following format:
		 * 
		 * byte Description 
		 * ------------- ------------------------------------------ 
		 * b0 Tags Found 
		 * b1 Type 1
		 * b5 Length 
		 * b6..Length NFCID
		 */

		int offset = medium.getOffsetBytes();

		if (pn532_packetbuffer[offset + 0] != 1) {
			log.warn("Problem byte 0 : {} != 1", pn532_packetbuffer[offset + 0]);
			return null;
		}
		
		if (pn532_packetbuffer[offset + 1] != 0x10) {
			log.warn("Problem byte 1 : {} != 0x10", pn532_packetbuffer[offset + 1]);
			//return null;
		}

		/* Card appears to be Mifare Classic */
		int uidLength = pn532_packetbuffer[offset + 7];
		byte[] buffer = new byte[uidLength];

		for (int i = 0; i < uidLength; i++) {
			buffer[i] = pn532_packetbuffer[offset + 8 + i];
		}

		return buffer;
	}
	
	/**************************************************************************/
	/*!
	    Sets the MxRtyPassiveActivation uint8_t of the RFConfiguration register
	    @param  maxRetries    0xFF to wait forever, 0x00..0xFE to timeout
	                          after mxRetries
	    @returns 1 if everything executed properly, 0 for an error
	*/
	/**
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws IllegalStateException 
	 ************************************************************************/
	public int setPassiveActivationRetries(byte maxRetries) throws IllegalStateException, InterruptedException, IOException {
				
		if (debug) log.debug("setPassiveActivationRetries {}", maxRetries);
		byte[] command = new byte[5];
		
		command[0] = PN532Constant.PN532_COMMAND_RFCONFIGURATION;
		command[1] = 5;    // Config item 5 (MaxRetries)
		command[2] = (byte)0xFF; // MxRtyATR (default = 0xFF)
		command[3] = 0x01; // MxRtyPSL (default = 0x01)
		command[4] = maxRetries;
		
		var result = medium.writeCommand(command);
		if (result != CommandStatus.OK) {
			log.warn("Error writing: {}", result);
			return 0; // command failed
		}

	    return medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
	}	
	
	/**
	 * 
	 * Read a PN532 register.
	 * 
	 * @param reg the 16-bit register address.
	 * @return The register value.
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public byte readRegister(int reg) throws IllegalStateException, InterruptedException, IOException {
		
		if (debug) log.debug("readRegister : {}", reg);
		
		byte[] command = new byte[3];
		command[0] = PN532Constant.PN532_COMMAND_READREGISTER;
		command[1] = (byte)((reg >> 8) & 0xFF);
	    command[2] = (byte)(reg & 0xFF);

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return 0;
		}

		return pn532_packetbuffer[0];
	}
	
	public byte powerDown() throws IllegalStateException, InterruptedException, IOException {
		if (debug) log.debug("powerDown()");
		
		byte[] command = new byte[2];
		command[0] = PN532Constant.PN532_COMMAND_POWERDOWN;
		command[1] = (byte)0xFB; // i2c/gpio/spi/hsu/rf/rfu=0/int1/int0
	    //command[2] = 0x00; // Optional: handel IRQ

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return 0;
		}

		return pn532_packetbuffer[0];
	}
	
	/**
	 * 
	 *  Write to a PN532 register.
	 *  
	 * @param reg the 16-bit register address.
	 * @param val the 8-bit value to write.
	 * @return 0 for failure, 1 for success.
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public int writeRegister(int reg, byte val) throws IllegalStateException, InterruptedException, IOException {
		
		if (debug) log.debug("writeRegister : {}", reg);
		
		byte[] command = new byte[4];
		command[0] = PN532Constant.PN532_COMMAND_WRITEREGISTER;
		command[1] = (byte)((reg >> 8) & 0xFF);
	    command[2] = (byte)(reg & 0xFF);
	    command[3] = val;

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return 0;
		}

		return pn532_packetbuffer[0];
	}	
	
	/**
	 * 
	 * Tries to authenticate a block of memory on a MIFARE card using the
     * INDATAEXCHANGE command.  See section 7.3.8 of the PN532 User Manual
     * for more information on sending MIFARE and other commands.
	 * 
	 * @param tag Tag Uid
	 * @param block The block number to authenticate.  (0..63 for
                          1KB cards, and 0..255 for 4KB cards).
	 * @param key  Key used for authentication
	 * @return
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public int authenticateBlock(Tag tag, int block, KeyValue key) throws IllegalStateException, InterruptedException, IOException {
				
		if (debug) log.debug("authenticateBlock : {}", block);
		
		releaseCard();
		readPassiveTargetID(PN532Constant.PN532_MIFARE_ISO14443A);
		
		var keyVal = key.getKeyValue();
		var tagVal = tag.getGeneralBytes();
		byte[] command = new byte[4+keyVal.length+4];
		command[0] = PN532Constant.PN532_COMMAND_INDATAEXCHANGE;
		command[1] = 1; /* Max. card number */
	    command[2] = (byte)((key.getKey() == PN532Constant.Key.A) ? PN532Constant.MIFARE_CMD_AUTH_A : PN532Constant.MIFARE_CMD_AUTH_B);
	    command[3] = (byte)block;
	    // Key
	    for (int i=0; i< keyVal.length; i++) {
	    	command[4+i] = keyVal[i];
	    }
	    // Uid
	    for (int i=0; i<4; i++) {
	    	command[4+keyVal.length+i] = tagVal[tagVal.length-4+i];
	    }

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		byte[] buffer = new byte[64];
		// read data packet
		int status = medium.readResponse(buffer, buffer.length);
		if (status < 0) {
			return 0;
		}
		
		// Check if the response is valid and we are authenticated???
	    // for an auth success it should be bytes 5-7: 0xD5 0x41 0x00
	    // Mifare auth error is technically byte 7: 0x14 but anything other and 0x00 is not good
	    if (buffer[0] == 0x14) {
	    	log.warn("Mifare auth error");
	    	return 0;
	    } else if (buffer[0] != 0x00) {
	        log.warn("Authentification failed");
	        return 0;
	    }
		
		return 1;
	}
	
	/**
	 * 
	 * Tries to write an entire 16-bytes data block at the specified block
     * address.
     * 
	 * @param block The block number to authenticate.  (0..63 for
                          1KB cards, and 0..255 for 4KB cards).
	 * @param data The byte array that contains the data to write.
	 * @return 1 if everything executed properly, 0 for an error
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public int writeBlock(int block, byte[] data) throws IllegalStateException, InterruptedException, IOException {
		
		if (debug) log.debug("writeBlock : {}", block);
		
		byte[] command = new byte[4+data.length];
		command[0] = PN532Constant.PN532_COMMAND_INDATAEXCHANGE;
		command[1] = 1; /* card numbers */
	    command[2] = PN532Constant.MIFARE_CMD_WRITE;
	    command[3] = (byte)block;
	    // Data
	    for (int i=0; i< data.length; i++) {
	    	command[4+i] = data[i];
	    }
	   
		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * 
	 * Tries to read an entire 16-bytes data block at the specified block
     * address.
	 * 
	 * @param block  The block number to read.  (0..63 for
                          1KB cards, and 0..255 for 4KB cards).
	 * @return Bytes read or null 
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public byte[] readBlock(int block) throws IllegalStateException, InterruptedException, IOException {
		
		if (debug) log.debug("readBlock : {}", block);
		
		byte[] command = new byte[4];
		command[0] = PN532Constant.PN532_COMMAND_INDATAEXCHANGE;
		command[1] = 1; /* card number */
	    command[2] = PN532Constant.MIFARE_CMD_READ;
	    command[3] = (byte)block;
	   
		if (medium.writeCommand(command) != CommandStatus.OK) {
			return null;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return null;
		}
		
	    /* If byte 8 isn't 0x00 we probably have an error */
	    if (pn532_packetbuffer[0] != 0x00) {
	        return null;
	    }
		
	    /* Copy the 16 data bytes to the output buffer        */
	    /* Block content starts at byte 9 of a valid response */
	    byte[] result = new byte[16];
	    for (int i=0; result.length<16; i++) {
	    	result[i] = pn532_packetbuffer[1+i];
	    }
	    
		return result;
	}	
	
	public byte[] readPage(int page) throws IllegalStateException, InterruptedException, IOException {
		
		if (debug) log.debug("readPage : {}", page);
		
		byte[] command = new byte[4];
	    /* Prepare the command */
		command[0] = PN532Constant.PN532_COMMAND_INDATAEXCHANGE;
		command[1] = 1;                   /* Card number */
		command[2] = PN532Constant.MIFARE_CMD_READ;     /* Mifare Read command = 0x30 */
		command[3] = (byte) page;                /* Page Number (0..63 in most cases) */

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return null;
		}
		
		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return null;
		}
		
	    /* If byte 8 isn't 0x00 we probably have an error */
	    if (pn532_packetbuffer[0] != 0x00) {
	        return null;
	    }
		
        /* Copy the 4 data bytes to the output buffer         */
        /* Block content starts at byte 9 of a valid response */
        /* Note that the command actually reads 16 bytes or 4  */
        /* pages at a time ... we simply discard the last 12  */
        /* bytes                                              */
	    byte[] result = new byte[4];
	    for (int i=0; result.length<4; i++) {
	    	result[i] = pn532_packetbuffer[1+i];
	    }

	    return result;
	}

	/**
	    Tries to write an entire 4-bytes data buffer at the specified page
	    address.
	    @param  page     The page number to write into.  (0..63).
	    @param  buffer   The byte array that contains the data to write.
	    @returns 1 if everything executed properly, 0 for an error
	*/
	public boolean writePage(int page, byte[] data) throws IllegalStateException, InterruptedException, IOException {
		if (debug) log.debug("writePage {} : {}", page, data);
		byte[] command = new byte[4+data.length];
	    /* Prepare the first command */
		command[0] = PN532Constant.PN532_COMMAND_INDATAEXCHANGE;
		command[1] = 1;                           /* Card number */
		command[2] = PN532Constant.MIFARE_CMD_WRITE_ULTRALIGHT; /* Mifare UL Write cmd = 0xA2 */
		command[3] = (byte) page;                        /* page Number (0..63) */
	    // Data
	    for (int i=0; i< data.length; i++) {
	    	command[4+i] = data[i];
	    }
	   
		if (medium.writeCommand(command) != CommandStatus.OK) {
			return false;
		}
	    
		// read data packet
		int status = medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length);
		if (status < 0) {
			return false;
		}
		
		return true;
	}	
	
	public int releaseCard() throws IllegalStateException, InterruptedException, IOException {
		if (debug) log.debug("releaseCard()");
		
		byte[] command = new byte[2];
		command[0] = PN532Constant.PN532_COMMAND_INRELEASE;
		command[1] = 0x00; /* All targets */

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		byte[] buffer = new byte[64];
		// read data packet
		int status = medium.readResponse(buffer, buffer.length);
		if (status < 0) {
			return 0;
		}
		
		return 1;
	}
	
	public int deselectCard() throws IllegalStateException, InterruptedException, IOException {
		if (debug) log.debug("deselectCard()");
		
		byte[] command = new byte[2];
		command[0] = PN532Constant.PN532_COMMAND_INDESELECT;
		command[1] = 0x00; /* All targets */

		if (medium.writeCommand(command) != CommandStatus.OK) {
			return 0;
		}

		byte[] buffer = new byte[64];
		// read data packet
		int status = medium.readResponse(buffer, buffer.length);
		if (status < 0) {
			return 0;
		}
		
		return 1;
	}	

	public static String getByteString(byte[] arr) {
		String output = "[";

		if (arr != null) {
			for (int i = 0; i < arr.length; i++) {
				output += Integer.toHexString(arr[i]) + " ";
			}
		}
		return output.trim() + "]";
	}
}