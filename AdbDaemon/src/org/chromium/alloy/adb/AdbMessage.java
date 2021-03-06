package org.chromium.alloy.adb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * TODO: Insert description here. (generated by penghuang)
 */
class AdbMessage {
  public static final int MAX_PAYLOAD = 4096;
  public static final int MAX_MESSAGE_SIZE = MAX_PAYLOAD + 6 * 4;

  public static final int A_SYNC = 0x434e5953;
  public static final int A_CNXN = 0x4e584e43;
  public static final int A_OPEN = 0x4e45504f;
  public static final int A_OKAY = 0x59414b4f;
  public static final int A_CLSE = 0x45534c43;
  public static final int A_WRTE = 0x45545257;
  public static final int A_AUTH = 0x48545541;

  public static final int A_VERSION = 0x01000000;

  public int command = 0;
  public int arg0 = 0;
  public int arg1 = 0;
  public int dataLength = 0;
  public int dataCheck = 0;
  public int magic = 0;
  public byte[] data = null;

  boolean needAppendZero = false;

  public AdbMessage() {
  }

  public AdbMessage(int command, int arg0, int arg1) {
    this.command = command;
    this.arg0 = arg0;
    this.arg1 = arg1;
  }

  public AdbMessage(int command, int arg0, int arg1, byte[] data) {
    this(command, arg0, arg1);
    this.data = data;
  }

  public AdbMessage(int command, int arg0, int arg1, String data) {
    this(command, arg0, arg1, data.getBytes());
    this.needAppendZero = true;
  }

  public static AdbMessage read(ByteBuffer buffer) throws IOException {
    if (buffer.remaining() < 24)
      return null;
    buffer.mark();
    AdbMessage message = new AdbMessage();
    message.command = buffer.getInt();
    message.arg0 = buffer.getInt();
    message.arg1 = buffer.getInt();
    message.dataLength = buffer.getInt();
    message.dataCheck = buffer.getInt();
    message.magic = buffer.getInt();
    message.checkHeader();

    if (buffer.remaining() < message.dataLength) {
      buffer.reset();
      return null;
    }

    message.data = new byte[message.dataLength];
    buffer.get(message.data);
    message.needAppendZero = false;
    message.checkData();
    return message;
  }

  public void setData(byte[] data) {
    this.data = data;
    this.needAppendZero = false;
  }

  public void setData(final String data) {
    this.data = data.getBytes();
    setData(data, true);
  }

  public void setData(final String data, boolean needAppendZero) {
    this.data = data.getBytes();
    this.needAppendZero = needAppendZero;
  }

  public ByteBuffer toByteBuffer() {
    prepare();
    ByteBuffer buffer = ByteBuffer.allocate(dataLength + 24);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    writeToByteBufferInternal(buffer);
    return buffer;
  }
  
  public void writeToByteBuffer(ByteBuffer buffer) {
    prepare();
    writeToByteBufferInternal(buffer);
  }
  
  private void writeToByteBufferInternal(ByteBuffer buffer) {
    buffer.putInt(command);
    buffer.putInt(arg0);
    buffer.putInt(arg1);
    buffer.putInt(dataLength);
    buffer.putInt(dataCheck);
    buffer.putInt(magic);
    if (data != null)
      buffer.put(data);
    if (needAppendZero)
      buffer.put((byte)0);
  }

  private void checkHeader() throws IOException {
    if (magic != (command ^ 0xffffffff))
      throw new IOException("Invalid magic.");

    if (dataLength > MAX_PAYLOAD)
      throw new IOException("Message data length > MAX_PAYLOAD.");
  }

  private void checkData() throws IOException {
    int s = sum();
    if (s != dataCheck) {
      throw new IOException("Message data checking failed.");
    }
  }

  private int sum() {
    long result = 0;
    if (data != null) {
      int count = data.length;
      while (count-- > 0)
        result += ((long)data[count]) & 0xff;
    }
    return (int)(result & 0xffffffff);
  }

  private void prepare() {
    dataLength = data != null ? data.length : 0;
    if (needAppendZero)
      dataLength += 1;
    dataCheck = sum();
    magic = command ^ 0xffffffff;
  }

  @Override
  public String toString() {
    String tag = null;
    switch (command) {
      case A_SYNC: tag = "SYNC"; break;
      case A_CNXN: tag = "CNXN" ; break;
      case A_OPEN: tag = "OPEN"; break;
      case A_OKAY: tag = "OKAY"; break;
      case A_CLSE: tag = "CLSE"; break;
      case A_WRTE: tag = "WRTE"; break;
      case A_AUTH: tag = "AUTH"; break;
      default: tag = "????"; break;
    }
    
    return String.format("%s %08x %08x %04x %s",
        tag, arg0, arg1, dataLength,
        command != A_WRTE ? new String(data) : new String(data, 0, Math.min(data.length, 16)));
  }
}
