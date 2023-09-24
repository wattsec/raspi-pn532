package mk.hsilomedus.pn532;

public class PN532Constant {
	
	public static final int    PN532_SERIAL_BAUD = 115200;
	public static final int    PN532_SERIAL_DATA = 8;
	public static final String PN532_SERIAL_PARITY = "NONE";
	public static final int    PN532_SERIAL_STOP = 1;
	public static final String PN532_SERIAL_FLOW = "NONE";
	
	public static final byte PN532_ACK[] = { 0, 0, (byte)0xFF, 0, (byte)0xFF, 0 };
	public static final byte PN532_NACK[] = { 0, 0, (byte)0xFF, (byte)0xFF, 0, 0 };
	public static final byte PN532_ERROR[] = { 0, 0, (byte)0xFF, 0x01, (byte)0xFF, 0x7F, (byte)0x81, 0 };
	public static final byte PN532_WEAKUP[] = { 0x55, 0x55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0//};
	 ,0 , 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	public static final byte PN532_DUMMY = 0x55;
	public static final long PN532_WAKEUP_TIME_MILLIS = 300;
	// PN532 Commands
	public static final byte PN532_COMMAND_DIAGNOSE =             (0x00);
	public static final byte PN532_COMMAND_GETFIRMWAREVERSION =   (0x02);
	public static final byte PN532_COMMAND_GETGENERALSTATUS =     (0x04);
	public static final byte PN532_COMMAND_READREGISTER =         (0x06);
	public static final byte PN532_COMMAND_WRITEREGISTER =        (0x08);
	public static final byte PN532_COMMAND_READGPIO =             (0x0C);
	public static final byte PN532_COMMAND_WRITEGPIO =            (0x0E);
	public static final byte PN532_COMMAND_SETSERIALBAUDRATE =    (0x10);
	public static final byte PN532_COMMAND_SETPARAMETERS =        (0x12);
	public static final byte PN532_COMMAND_SAMCONFIGURATION =     (0x14);
	public static final byte PN532_COMMAND_POWERDOWN =            (0x16);
	public static final byte PN532_COMMAND_RFCONFIGURATION =      (0x32);
	public static final byte PN532_COMMAND_RFREGULATIONTEST =     (0x58);
	public static final byte PN532_COMMAND_INJUMPFORDEP =         (0x56);
	public static final byte PN532_COMMAND_INJUMPFORPSL =         (0x46);
	public static final byte PN532_COMMAND_INLISTPASSIVETARGET =  (0x4A);
	public static final byte PN532_COMMAND_INATR =                (0x50);
	public static final byte PN532_COMMAND_INPSL =                (0x4E);
	public static final byte PN532_COMMAND_INDATAEXCHANGE =       (0x40);
	public static final byte PN532_COMMAND_INCOMMUNICATETHRU =    (0x42);
	public static final byte PN532_COMMAND_INDESELECT =           (0x44);
	public static final byte PN532_COMMAND_INRELEASE =            (0x52);
	public static final byte PN532_COMMAND_INSELECT =             (0x54);
	public static final byte PN532_COMMAND_INAUTOPOLL =           (0x60);
	public static final byte PN532_COMMAND_TGINITASTARGET =       (byte)(0x8C);
	public static final byte PN532_COMMAND_TGSETGENERALBYTES =    (byte)(0x92);
	public static final byte PN532_COMMAND_TGGETDATA =            (byte)(0x86);
	public static final byte PN532_COMMAND_TGSETDATA =            (byte)(0x8E);
	public static final byte PN532_COMMAND_TGSETMETADATA =        (byte)(0x94);
	public static final byte PN532_COMMAND_TGGETINITIATORCOMMAND= (byte)(0x88);
	public static final byte PN532_COMMAND_TGRESPONSETOINITIATOR= (byte)(0x90);
	public static final byte PN532_COMMAND_TGGETTARGETSTATUS =    (byte)(0x8A);

	public static final byte PN532_RESPONSE_INDATAEXCHANGE =      (0x41);
	public static final byte PN532_RESPONSE_INLISTPASSIVETARGET = (0x4B);


	public static final byte PN532_MIFARE_ISO14443A =             (0x00);

	// Mifare Commands
	public static final byte MIFARE_CMD_AUTH_A =                  (0x60);
	public static final byte MIFARE_CMD_AUTH_B =                  (0x61);
	public static final byte MIFARE_CMD_READ =                    (0x30);
	public static final byte MIFARE_CMD_WRITE =             (byte)(0xA0);
	public static final byte MIFARE_CMD_WRITE_ULTRALIGHT =  (byte)(0xA2);
	public static final byte MIFARE_CMD_TRANSFER =          (byte)(0xB0);
	public static final byte MIFARE_CMD_DECREMENT =         (byte)(0xC0);
	public static final byte MIFARE_CMD_INCREMENT =         (byte)(0xC1);
	public static final byte MIFARE_CMD_STORE =             (byte)(0xC2);

	// FeliCa Commands
	public static final byte FELICA_CMD_POLLING  =                (0x00);
	public static final byte FELICA_CMD_REQUEST_SERVICE =         (0x02);
	public static final byte FELICA_CMD_REQUEST_RESPONSE =        (0x04);
	public static final byte FELICA_CMD_READ_WITHOUT_ENCRYPTION = (0x06);
	public static final byte FELICA_CMD_WRITE_WITHOUT_ENCRYPTION =(0x08);
	public static final byte FELICA_CMD_REQUEST_SYSTEM_CODE =     (0x0C);

	// Prefixes for NDEF Records (to identify record type)
	public static final byte NDEF_URIPREFIX_NONE =                (0x00);
	public static final byte NDEF_URIPREFIX_HTTP_WWWDOT =         (0x01);
	public static final byte NDEF_URIPREFIX_HTTPS_WWWDOT =        (0x02);
	public static final byte NDEF_URIPREFIX_HTTP =                (0x03);
	public static final byte NDEF_URIPREFIX_HTTPS =               (0x04);
	public static final byte NDEF_URIPREFIX_TEL =                 (0x05);
	public static final byte NDEF_URIPREFIX_MAILTO =              (0x06);
	public static final byte NDEF_URIPREFIX_FTP_ANONAT =          (0x07);
	public static final byte NDEF_URIPREFIX_FTP_FTPDOT =          (0x08);
	public static final byte NDEF_URIPREFIX_FTPS =                (0x09);
	public static final byte NDEF_URIPREFIX_SFTP =                (0x0A);
	public static final byte NDEF_URIPREFIX_SMB =                 (0x0B);
	public static final byte NDEF_URIPREFIX_NFS =                 (0x0C);
	public static final byte NDEF_URIPREFIX_FTP =                 (0x0D);
	public static final byte NDEF_URIPREFIX_DAV =                 (0x0E);
	public static final byte NDEF_URIPREFIX_NEWS =                (0x0F);
	public static final byte NDEF_URIPREFIX_TELNET =              (0x10);
	public static final byte NDEF_URIPREFIX_IMAP =                (0x11);
	public static final byte NDEF_URIPREFIX_RTSP =                (0x12);
	public static final byte NDEF_URIPREFIX_URN =                 (0x13);
	public static final byte NDEF_URIPREFIX_POP =                 (0x14);
	public static final byte NDEF_URIPREFIX_SIP =                 (0x15);
	public static final byte NDEF_URIPREFIX_SIPS =                (0x16);
	public static final byte NDEF_URIPREFIX_TFTP =                (0x17);
	public static final byte NDEF_URIPREFIX_BTSPP =               (0x18);
	public static final byte NDEF_URIPREFIX_BTL2CAP =             (0x19);
	public static final byte NDEF_URIPREFIX_BTGOEP =              (0x1A);
	public static final byte NDEF_URIPREFIX_TCPOBEX =             (0x1B);
	public static final byte NDEF_URIPREFIX_IRDAOBEX =            (0x1C);
	public static final byte NDEF_URIPREFIX_FILE =                (0x1D);
	public static final byte NDEF_URIPREFIX_URN_EPC_ID =          (0x1E);
	public static final byte NDEF_URIPREFIX_URN_EPC_TAG =         (0x1F);
	public static final byte NDEF_URIPREFIX_URN_EPC_PAT =         (0x20);
	public static final byte NDEF_URIPREFIX_URN_EPC_RAW =         (0x21);
	public static final byte NDEF_URIPREFIX_URN_EPC =             (0x22);
	public static final byte NDEF_URIPREFIX_URN_NFC =             (0x23);

	public static final byte PN532_GPIO_VALIDATIONBIT =     (byte)(0x80);
	public static final byte PN532_GPIO_P30 =                     (0);
	public static final byte PN532_GPIO_P31 =                     (1);
	public static final byte PN532_GPIO_P32 =                     (2);
	public static final byte PN532_GPIO_P33 =                     (3);
	public static final byte PN532_GPIO_P34 =                     (4);
	public static final byte PN532_GPIO_P35 =                     (5);

	// FeliCa consts
	public static final byte FELICA_READ_MAX_SERVICE_NUM =        16;
	public static final byte FELICA_READ_MAX_BLOCK_NUM =          12; // for typical FeliCa card
	public static final byte FELICA_WRITE_MAX_SERVICE_NUM =       16;
	public static final byte FELICA_WRITE_MAX_BLOCK_NUM =         10; // for typical FeliCa card
	public static final byte FELICA_REQ_SERVICE_MAX_NODE_NUM =    32;	
}
