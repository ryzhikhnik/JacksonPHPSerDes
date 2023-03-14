package PHPSerDes;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

public class ByteArrayOutputStreamWriter extends OutputStreamWriter {
    private final ByteArrayOutputStream _byteArrayOutputStream;

    public ByteArrayOutputStreamWriter(ByteArrayOutputStream _byteArrayOutputStream) {
        super(_byteArrayOutputStream);
        this._byteArrayOutputStream = _byteArrayOutputStream;
    }

    public String toString() {
        return _byteArrayOutputStream.toString();
    }
}