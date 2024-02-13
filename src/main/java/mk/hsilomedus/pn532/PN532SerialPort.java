package mk.hsilomedus.pn532;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PN532SerialPort implements IPN532Interface {
	
	
	public static final int READ_TIMEOUT = 1000; //ms

	private static final int PN532_TIMEOUT = -2;
	private static final int PN532_INVALID_FRAME = -3;
	private static final int PN532_NO_SPACE = -4;

	SerialPort serial = null;
	int baud = PN532Constant.PN532_SERIAL_BAUD;
	private byte command;
	
	@Setter
	boolean debug = true;
	@Setter 
	boolean trace = false;

	public PN532SerialPort() {
	}
	
	public boolean initSerial(String portName, int baud, String parity, int data, int stop) {
		log.info("initSerial: {}:{}:{}:{}:{}", portName, baud, parity, data, stop);	
		
		SerialPort[] serials = SerialPort.getCommPorts();
		for (SerialPort port : serials) {
			log.info("Port : " + port.getSystemPortName());
		}
		
		this.baud = baud;
		
		if (portName != null && !portName.isBlank()) {
			serial = SerialPort.getCommPort(portName);
		} else {
			if (serials == null || serials.length < 1) {
				log.warn("No serial port!!!!");
			} else {
				serial = serials[0];
			}
		}
				
		if (serial != null) {
			return openSerialPort();
		} else {
			log.warn("No CommPort!!");
			return false;
		}		
	}
	
	private boolean openSerialPort() {
		if (serial != null) {
			log.info("Port : {} / {} / {}", serial.getSystemPortName(), serial.getPortDescription(), serial.getDescriptivePortName());
			log.info("Buffer R/W : {} / {}", serial.getDeviceReadBufferSize(), serial.getDeviceWriteBufferSize());
			
			serial.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
			
			if (baud > 0) {
				serial.setBaudRate(baud);
			}
			
			switch (PN532Constant.PN532_SERIAL_PARITY) {
			case "NONE":
				serial.setParity(SerialPort.NO_PARITY);
				break;

			case "ODD":
				serial.setParity(SerialPort.ODD_PARITY);
				break;

			case "EVEN":
				serial.setParity(SerialPort.EVEN_PARITY);
				break;

			case "MARK":
				serial.setParity(SerialPort.MARK_PARITY);
				break;

			case "SPACE":
				serial.setParity(SerialPort.SPACE_PARITY);
				break;

			default:
				log.warn("Unknown parity: {}", PN532Constant.PN532_SERIAL_BAUD);
				break;
			}
				
			
			if (PN532Constant.PN532_SERIAL_DATA >= 0) {
				serial.setNumDataBits(PN532Constant.PN532_SERIAL_DATA);
			}
			if (PN532Constant.PN532_SERIAL_STOP >= 0) {
				serial.setNumStopBits(PN532Constant.PN532_SERIAL_STOP);
			}
			serial.openPort();		
			//serial.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED);
			
			log.info("Port Open: {} - {} - {} - {} - {}", serial.isOpen(), serial.getBaudRate(), serial.getParity(), serial.getNumDataBits(), serial.getNumStopBits());
			
			return true;
		} else {
			log.warn("No commPort");
			return false;
		}
	}

	@Override
	public void begin() throws IOException, UnsupportedBoardType, InterruptedException {
		if (debug) log.debug("PN532SerialPort.begin()");

		/*
		// ScottyDoesKnow: everything here but speed should be defaults, but might as well not assume
		SerialConfig config = new SerialConfig();
		log.info("SerialPort:" + SerialPort.getDefaultPort());
		config.device(SerialPort.getDefaultPort())
			.baud(Baud._115200)
			.dataBits(DataBits._8)
			.parity(Parity.NONE)
			.stopBits(StopBits._1)
			.flowControl(FlowControl.NONE);
		
		serial.open(config);
		*/
	}

	@Override
	public void wakeup() throws IllegalStateException, IOException {
		if (debug) log.debug("PN532SerialPort.wakeup()");
		
		for (byte b: PN532Constant.PN532_WEAKUP) {
			write(b);
		}
		
		//serial.flush();
		
		dumpSerialBuffer();
	}
	
	public void doWakeup() throws IllegalStateException, IOException {
		
//		List<Byte> buffer = new ArrayList<>();
//		buffer.add(PN532Constant.PN532_DUMMY);
//		buffer.add(PN532Constant.PN532_DUMMY);
//		//write(PN532Constant.PN532_DUMMY);
//		//write(PN532Constant.PN532_DUMMY);
//		/*
//		try {
//			log.info("Sleeping {} ms", PN532Constant.PN532_WAKEUP_TIME_MILLIS);
//			Thread.sleep(PN532Constant.PN532_WAKEUP_TIME_MILLIS);
//		} catch (InterruptedException e) {
//			log.warn("Problem wake up sleeping");
//		}
//		*/
//		for (int i=0; i<5; i++) {
//			//write((byte)0x00);
//			buffer.add((byte)0x00);
//		}
//		write(buffer);
		
		write(PN532Constant.PN532_WEAKUP);
	}
	
	/**
	 * Send ACK message to PN532
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void sendAck() throws IllegalStateException, IOException {
		if (debug) log.debug("Send ACK");
		write(PN532Constant.PN532_ACK);
	}

	@Override
	public CommandStatus writeCommand(byte[] header, byte[] body) throws InterruptedException, IllegalStateException, IOException {
		if (debug) log.debug("PN532SerialPort.writeCommand({}, {})", PN532Constant.toStringHex(header), (body != null ? PN532Constant.toStringHex(body) : "--"));

		dumpSerialBuffer();
		
		doWakeup();
		
		command = header[0];
		List<Byte> buffer = new ArrayList<>();

		//write(PN532_PREAMBLE);
		//write(PN532_STARTCODE1);
		//write(PN532_STARTCODE2);
		buffer.add(PN532_PREAMBLE);
		buffer.add(PN532_STARTCODE1);
		buffer.add(PN532_STARTCODE2);

		int length = header.length + (body != null ? body.length : 0) + 1;
		//write((byte) length);
		//write((byte) (~length + 1));
		buffer.add((byte) length);
		buffer.add((byte) (~length + 1));

		//write(PN532_HOSTTOPN532);
		buffer.add(PN532_HOSTTOPN532);
		byte sum = PN532_HOSTTOPN532;
		
		write(buffer);

		write(header);
		for (int i = 0; i < header.length; i++) {
			sum += header[i];
		}

		if (body != null) {
			write(body);
			for (int i = 0; i < body.length; i++) {
				sum += body[i];
			}
		}

		byte checksum = (byte) (~sum + 1);
		//write(checksum);
		//write(PN532_POSTAMBLE);
		
		buffer = new ArrayList<>();
		
		buffer.add(checksum);
		buffer.add(PN532_POSTAMBLE);
		write(buffer);
				
		//serial.flush();
		
		return readAckFrame();
	}

	@Override
	public CommandStatus writeCommand(byte header[]) throws InterruptedException, IllegalStateException, IOException {
		return writeCommand(header, null);
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException, IllegalStateException, IOException {
		if (trace) log.trace("PN532SerialPort.readResponse(..., {}, {})", expectedLength, timeout);
		
		byte[] tmp = new byte[3];
		if (receive(tmp, 3, timeout) <= 0) {
			log.warn("response timeout 1");
			return PN532_TIMEOUT;
		}
		if ((byte) 0 != tmp[0] || (byte) 0 != tmp[1] || (byte) 0xFF != tmp[2]) {
			log.warn("response invalid frame 1");
			return PN532_INVALID_FRAME;
		}

		byte[] length = new byte[2];
		if (receive(length, 2, timeout) <= 0) {
			log.warn("response timeout 2");
			return PN532_TIMEOUT;
		}
		if (0 != length[0] + length[1]) {
			log.warn("response invalid frame 2");
			return PN532_INVALID_FRAME;
		}
		length[0] -= 2;
		if (length[0] > expectedLength) {
			log.warn("response no space");
			return PN532_NO_SPACE;
		}

		byte cmd = (byte) (command + 1); // response command
		if (receive(tmp, 2, timeout) <= 0) {
			log.warn("response time out 3");
			return PN532_TIMEOUT;
		}
		if (PN532_PN532TOHOST != tmp[0] || cmd != tmp[1]) {
			log.warn("response invalid frame 3");
			return PN532_INVALID_FRAME;
		}

		if (length[0] > 0) {
			if (receive(buffer, length[0], timeout) != length[0]) {
				log.warn("response time out 4");
				return PN532_TIMEOUT;
			}
		}
		byte sum = (byte) (PN532_PN532TOHOST + cmd);
		for (int i = 0; i < length[0]; i++) {
			sum += buffer[i];
		}
		
		/** checksum and postamble */
		if (receive(tmp, 2, timeout) <= 0) {
			log.warn("response time out 5");
			return PN532_TIMEOUT;
		}
		if (0 != (sum + tmp[0]) || 0 != tmp[1]) {
			log.warn("response invalid frame 5");
			return PN532_INVALID_FRAME;
		}
		
		sendAck();

		return length[0];
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength) throws InterruptedException, IllegalStateException, IOException {
		return readResponse(buffer, expectedLength, READ_TIMEOUT);
	}

	private CommandStatus readAckFrame() throws InterruptedException, IllegalStateException, IOException {
		if (debug) log.debug("PN532SerialPort.readAckFrame()");
		
		byte ackBuf[] = new byte[PN532Constant.PN532_ACK.length];

		if (receive(ackBuf, PN532Constant.PN532_ACK.length) <= 0) {
			log.warn("PN532SerialPort.readAckFrame() Timeout");
			return CommandStatus.TIMEOUT;
		}

		for (int i = 0; i < ackBuf.length; i++) {
			if (ackBuf[i] != PN532Constant.PN532_ACK[i]) {
				log.warn("PN532SerialPort.readAckFrame() Invalid");
				return CommandStatus.INVALID_ACK;
			}
		}

		if (debug) log.debug("PN532SerialPort.readAckFrame() Success");
		return CommandStatus.OK;
	}

	int receive(byte[] buffer, int expectedLength, int timeout) throws InterruptedException, IllegalStateException, IOException {
		if (trace) log.trace("PN532SerialPort.receive(..., {}, {})",expectedLength ,timeout);

		int bufferIndex = 0;
		boolean receivedData;
		long startMs = 0;
		long endMs = 0;

		if (expectedLength > 0) {
			startMs = System.currentTimeMillis();
			if (trace) log.trace("Start reading: {}", startMs);
			receivedData = false;
			do {
				int avail = serial.bytesAvailable();
				if (avail == -1) {
					log.warn("Connection not opened");
					throw new IOException();
				} else if (avail == 0) {
					Thread.sleep(100);
				} else {
					buffer[bufferIndex++] = read();
					receivedData = true;
					//break;
				}
				endMs = System.currentTimeMillis();
			} while ((timeout == 0 || (((endMs - startMs) < timeout) && bufferIndex < expectedLength)));

			if (trace) log.trace("End Reading {} - {} = {}", startMs, endMs, endMs - startMs);
			
			if (bufferIndex <= 0) {
				log.warn("Timeout while reading.");
				return PN532_TIMEOUT;
			}
		} else {
			log.warn("Expected length: {}", expectedLength);
		}
		
		if (bufferIndex < expectedLength) {
			log.warn("BufferdIndex {} < {}", bufferIndex, expectedLength);
		}
		
		if (bufferIndex > 0) {		
			if (trace) log.trace("PN532SerialPort.recevice(): {}",PN532Constant.toStringHex(buffer,0, bufferIndex, true));
		} else {
			if (trace) log.trace("PN532SerialPort.recevice(): {}", "Empty");
		}
		
		return bufferIndex;
	}

	int receive(byte[] buffer, int expectedLength) throws InterruptedException, IllegalStateException, IOException {
		if (trace) log.trace("PN532SerialPort.recive() {}", expectedLength);
		return receive(buffer, expectedLength, READ_TIMEOUT);
	}

	private void write(byte toSend) throws IllegalStateException, IOException {
		if (trace) log.trace("PN532SerialPort.write() {}",  PN532Constant.toStringHex(toSend));

		serial.writeBytes(new byte[] {toSend}, 1);
	}

	private void write(byte[] toSend) throws IllegalStateException, IOException {
		if (trace) log.trace("PN532SerialPort.write() {}", PN532Constant.toStringHex(toSend));

		serial.writeBytes(toSend, toSend.length);
	}
	
	private void write(List<Byte> buffer) throws IllegalStateException, IOException {
		
		byte[] ret = new byte[buffer.size()];
	    int i=0;
	    for (byte b: buffer) {
	        ret[i++] = b;
	    }
		write(ret);
	}
	
	private byte read() throws IllegalStateException, IOException {
		byte[] readBuffer = new byte[1];
	    int numRead = serial.readBytes(readBuffer, readBuffer.length);
		byte result = readBuffer[0];
		if (trace) log.trace("PN532SerialPort.receive() {}: {}", numRead, PN532Constant.toStringHex(result));
		return result;
	}

	private void dumpSerialBuffer() throws IllegalStateException, IOException {
		if (debug) log.debug("PN532SerialPort.dumpSerialBuffer()");

		// #J: 
		//serial.discardInput();
		
		while (serial.bytesAvailable() > 0) {
			if (trace) log.trace("Dumping byte");
			read();
		}
	}

	@Override
	public int getOffsetBytes() {
		return 0;
	}
}