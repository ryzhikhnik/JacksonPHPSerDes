package PHPSerDes;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.json.JsonWriteContext;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.atomic.AtomicInteger;

public class PHPJsonGenerator extends GeneratorBase {

    private static final String DELIM = ":";
    private static final String SEMICOLON = ";";
    private final Writer _writer;
    private PHPWriter _context;

    PHPJsonGenerator(int features, ObjectCodec codec, Writer out) {
        super(features, codec);
        this._writer = out;
        this._context = PHPWriter.createRootContext(out);
    }

    @Override
    public Object getOutputTarget() {
        return this._writer;
    }

    @Override
    public void writeStartArray() throws IOException {
        if (_context.inArray()) {
            writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        _context = _context.createChildArrayContext();
    }

    @Override
    public void writeEndArray() throws IOException {
        if (!_context.inArray()) {
            _reportError("Current context is not ARRAY " + _context.getTypeDesc());
        }
        this.fillArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        if (_context.inArray()) {
            writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        _context = _context.createChildObjectContext();
    }

    @Override
    public void writeEndObject() throws IOException {
        if (!_context.inObject()) {
            _reportError("Current context is not OBJECT " + _context.getTypeDesc());
        }
        this.fillArray();
    }

    private void fillArray() throws IOException {
        this._context.getWriter().close();
        Writer writer = _context.getParent().getWriter();
        writer.append("a");
        writer.append(DELIM);
        writer.append(String.valueOf(_context.getFieldCount()));
        writer.append(DELIM);
        writer.append("{");
        writer.append(this._context.getWriter().toString());
        writer.append("}");
        _context = _context.getParent();
    }

    @Override
    public void writeFieldName(String s) throws IOException {
        int status = _context.writeFieldName(s);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("VALUE EXPECTED");
        }
        boolean isNum = true;
        if (s.length() == 0) {
            isNum = false;
        }
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                isNum = false;
                break;
            }
        }
        if (isNum) {
            writeNumber(s);
        } else {
            writeString(s);
        }
    }

    @Override
    public void writeString(String s) throws IOException {
        if (_context.inArray()) {
            writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        AtomicInteger byteCount = new AtomicInteger();

        s.chars().forEach((item) -> {
            if ((item >= 0x0001) && (item <= 0x007F)) {
                byteCount.getAndIncrement();
            } else if (item > 0x07FF) {
                byteCount.addAndGet(3);
            } else {
                byteCount.addAndGet(2);
            }
        });

        _context.append("s");
        _context.append(DELIM);
        _context.append(String.valueOf(byteCount));
        _context.append(DELIM);
        _context.append("\"");
        _context.append(s);
        _context.append("\"");
        _context.append(SEMICOLON);
        _context.writeValue();
    }

    private void writeArrayIndex(int index) throws IOException {
        _context.append("i");
        _context.append(DELIM);
        _context.append(String.valueOf(index));
        _context.append(SEMICOLON);
    }

    @Override
    public void writeString(char[] chars, int i, int i1) throws IOException {
        writeString(new String(chars).substring(i, i1));
    }

    @Override
    public void writeRawUTF8String(byte[] bytes, int i, int i1) {
    }

    @Override
    public void writeUTF8String(byte[] bytes, int i, int i1) {
    }

    @Override
    public void writeRaw(String s) {
    }

    @Override
    public void writeRaw(String s, int i, int i1) {
    }

    @Override
    public void writeRaw(char[] chars, int i, int i1) {
    }

    @Override
    public void writeRaw(char c) {
    }

    @Override
    public void writeBinary(Base64Variant base64Variant, byte[] bytes, int i, int i1) {
    }

    @Override
    public void writeNumber(int i) throws IOException {
        this.numberWrite(i);
    }

    @Override
    public void writeNumber(long l) throws IOException {
        this.numberWrite(l);
    }

    @Override
    public void writeNumber(BigInteger bigInteger) throws IOException {
        this.numberWrite(bigInteger);
    }

    private void numberWrite(Number number) throws IOException {
        if (_context.inArray()) {
            writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        _context.append("i");
        _context.append(DELIM);
        _context.append(String.valueOf(number));
        _context.append(SEMICOLON);
        _context.writeValue();
    }

    @Override
    public void writeNumber(double v) throws IOException {
        this.doubleWrite(v);
    }

    @Override
    public void writeNumber(float v) throws IOException {
        this.doubleWrite(v);
    }

    @Override
    public void writeNumber(BigDecimal bigDecimal) throws IOException {
        this.doubleWrite(bigDecimal);
    }

    private void doubleWrite(Number number) throws IOException {
        if (_context.inArray()) {
            this.writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        StringBuilder stringBuilder = new StringBuilder(String.valueOf(number));
        int idx = stringBuilder.indexOf("E");
        if (idx > -1) {
            int multy = Integer.parseInt(stringBuilder.substring(idx + 1));
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#", decimalFormatSymbols);
            df.setMaximumFractionDigits(multy);
            stringBuilder = new StringBuilder(df.format(number));
        }

        _context.append("d");
        _context.append(DELIM);
        _context.append(stringBuilder.toString());
        _context.append(SEMICOLON);
        _context.writeValue();
    }

    @Override
    public void writeNumber(String s) throws IOException {
        numberWrite(Long.parseLong(s));
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        if (_context.inArray()) {
            this.writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        _context.append("b");
        _context.append(DELIM);
        _context.append(b ? "1" : "0");
        _context.append(SEMICOLON);
        _context.writeValue();
    }

    @Override
    public void writeNull() throws IOException {
        if (_context.inArray()) {
            this.writeArrayIndex(_context.getFieldCount());
            _context.incrementFieldCount();
        }
        _context.append("N");
        _context.append(SEMICOLON);
        _context.writeValue();
    }

    @Override
    public void flush() throws IOException {
        _writer.flush();
    }

    @Override
    protected void _releaseBuffers() {
    }

    @Override
    protected void _verifyValueWrite(String s) {
    }
}