package PHPSerDes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.util.TextBuffer;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PHPJsonParser extends ParserMinimalBase {
    private static final Version VERSION = VersionUtil.parseVersion("1.0.0", "org.example", "JacksonPHPSerDes");

    protected Reader _reader;
    private ObjectCodec _objectCodec;
    private IOContext _ioContext = null;
    private TextBuffer _textBuffer = null;
    protected boolean _closed;
    private String _currentValue;
    protected JsonReadContext _parsingContext;
    private final List<JsonToken> _tokenList = new ArrayList<>();
    private int _idxToken = 0;
    private final List<String> _currentValueList = new ArrayList<>();
    private int _idxValue = 0;
    private CustomDeserializer _customDeserializer;
    private final List<JsonToken> _goodTokens = new ArrayList<>();

    public PHPJsonParser(IOContext _ioContext, Reader reader, int _parserFeatures, ObjectCodec objectCodec) {
        super(_parserFeatures);
        this._ioContext = _ioContext;
        this._textBuffer = _ioContext.constructTextBuffer();
        this._objectCodec = objectCodec;
        this._reader = reader;
        this._parsingContext = JsonReadContext.createRootContext(null);
        this.addNecessaryTokens();
        try {
            this._customDeserializer = new CustomDeserializer(reader, _tokenList, _currentValueList);
            this._customDeserializer.deserialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PHPJsonParser() {
    }

    @Override
    public JsonToken nextToken() {
        if (this._closed || this._tokenList.size() == 0) {
            return null;
        }
        this._currToken = this._tokenList.get(_idxToken++);
        if (_goodTokens.contains(this._currToken)) {
            this._currentValue = this._currentValueList.get(_idxValue++);
        }
        return this._currToken;
    }

    @Override
    public boolean isExpectedStartArrayToken() {
        JsonToken nextToken = this._tokenList.get(_idxToken);
        if (this._currToken == JsonToken.START_OBJECT && nextToken == JsonToken.END_OBJECT) {
            this._currToken = JsonToken.START_ARRAY;
            this._tokenList.set((_idxToken - 1), JsonToken.START_ARRAY);
            this._tokenList.set(_idxToken, JsonToken.END_ARRAY);
        }
        return this._currToken == JsonToken.START_ARRAY;
    }

    @Override
    public JsonParser skipChildren() throws IOException {
        if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
            return this;
        } else {
            int open = 1;
            while (true) {
                if (_idxToken > (_tokenList.size() - 1)) {
                    return this;
                }
                this._currToken = this._tokenList.get(_idxToken++);
                if (this._currToken.isStructStart()) {
                    ++open;
                } else if (this._currToken.isStructEnd()) {
                    --open;
                    if (open == 0) {
                        return this;
                    }
                } else {
                    _idxValue++;
                }
            }
        }
    }

    protected void _reportUnexpectedChar(int got, int expected) throws JsonParseException {
        _reportError("Expected " + ((char) expected) + ", but got '" + ((char) got) + "'");
    }

    private void addNecessaryTokens() {
        _goodTokens.add(JsonToken.VALUE_NUMBER_INT);
        _goodTokens.add(JsonToken.VALUE_NUMBER_FLOAT);
        _goodTokens.add(JsonToken.VALUE_TRUE);
        _goodTokens.add(JsonToken.VALUE_FALSE);
        _goodTokens.add(JsonToken.VALUE_NULL);
        _goodTokens.add(JsonToken.VALUE_STRING);
    }

    @Override
    protected void _handleEOF() {
    }

    @Override
    public String getCurrentName() {
        return this._currentValueList.get(_idxValue++);
    }

    @Override
    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    @Override
    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void close() throws IOException {
        if (!this._closed) {
            this._closed = true;
            try {
                this._reader.close();
            } finally {
                this._textBuffer.releaseBuffers();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return this._closed;
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return this._parsingContext;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return new JsonLocation(_ioContext == null ? null : _ioContext.contentReference(), 0, 0, 0);
    }

    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }

    @Override
    public void overrideCurrentName(String s) {
        if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
            this._currentValueList.set(_idxValue, s);
        }
    }

    @Override
    public String getText() {
        return this._currentValue;
    }

    @Override
    public char[] getTextCharacters() {
        return this._currentValue.toCharArray();
    }

    @Override
    public boolean hasTextCharacters() {
        return _currentValue != null && _currentValue.length() > 0;
    }

    @Override
    public Number getNumberValue() {
        if (this._currentValue.contains(".")) {
            return Double.parseDouble(this._currentValue);
        }
        return Long.parseLong(this._currentValue);
    }

    @Override
    public NumberType getNumberType() {
        if (this._currToken == JsonToken.VALUE_NUMBER_INT) {
            return NumberType.INT;
        } else if (_currToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return NumberType.FLOAT;
        }
        return null;
    }

    @Override
    public int getIntValue() {
        if (this._currentValue.contains(".")) {
            return (int) Double.parseDouble(this._currentValue);
        }
        return Integer.parseInt(this._currentValue);
    }

    @Override
    public long getLongValue() {
        if (this._currentValue.contains(".")) {
            return (long) Double.parseDouble(this._currentValue);
        }
        return Long.parseLong(this._currentValue);
    }

    @Override
    public BigInteger getBigIntegerValue() {
        return new BigInteger(this._currentValue);
    }

    @Override
    public float getFloatValue() {
        return Float.parseFloat(this._currentValue);
    }

    @Override
    public double getDoubleValue() {
        return Double.parseDouble(this._currentValue);
    }

    @Override
    public BigDecimal getDecimalValue() {
        return new BigDecimal(this._currentValue);
    }

    @Override
    public int getTextLength() {
        return this._currentValue.length();
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    public byte[] getBinaryValue(Base64Variant base64Variant) {
        return new byte[0];
    }

    @Override
    public String getValueAsString() {
        return _currentValue;
    }
}