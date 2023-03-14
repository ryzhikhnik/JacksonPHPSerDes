package PHPSerDes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

public class PHPWriter {

    private final JsonWriteContext _jsonContext;
    private final PHPWriter _parent;
    private Writer _writer;
    private PHPWriter _childArray;
    private int _fieldCount = 0;

    protected PHPWriter(JsonWriteContext _jsonContext, PHPWriter _parent, Writer _writer) {
        this._jsonContext = _jsonContext;
        this._parent = _parent;
        this._writer = _writer;
        if (this._writer == null) {
            this._writer = new ByteArrayOutputStreamWriter(new ByteArrayOutputStream());
        }
    }

    public static PHPWriter createRootContext(Writer writer) {
        return new PHPWriter(JsonWriteContext.createRootContext(null), null, writer);
    }

    public int writeFieldName(String s) throws JsonProcessingException {
        _fieldCount++;
        return _jsonContext.writeFieldName(s);
    }

    public boolean inArray() {
        return _jsonContext.inArray();
    }

    public boolean inObject() {
        return _jsonContext.inObject();
    }

    public void append(CharSequence charSequence) throws IOException {
        _writer.append(charSequence);
    }

    public int writeValue() {
        return _jsonContext.writeValue();
    }

    public PHPWriter createChildArrayContext() {
        _childArray = new PHPWriter(_jsonContext.createChildArrayContext(), this, null);
        return _childArray;
    }

    public PHPWriter createChildObjectContext() {
        _childArray = new PHPWriter(_jsonContext.createChildObjectContext(), this, null);
        return _childArray;
    }

    public int getFieldCount() {
        return this._fieldCount;
    }

    public void incrementFieldCount() {
        this._fieldCount++;
    }

    public String getTypeDesc() {
        return _jsonContext.typeDesc();
    }

    public PHPWriter getParent() {
        return this._parent;
    }
    public Writer getWriter() {
        return this._writer;
    }
}