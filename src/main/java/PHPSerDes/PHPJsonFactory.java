package PHPSerDes;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PHPJsonFactory extends JsonFactory {

    public static final String FORMAT_NAME_JSON = "PHP";

    @Override
    public String getFormatName() {
        return FORMAT_NAME_JSON;
    }

    @Override
    public MatchStrength hasFormat(InputAccessor acc) {
        return MatchStrength.FULL_MATCH;
    }

    @Override
    public PHPJsonGenerator createGenerator(OutputStream out) throws UnsupportedEncodingException {
        return this.createGenerator(out, JsonEncoding.UTF8);
    }

    @Override
    public PHPJsonGenerator createGenerator(OutputStream out, JsonEncoding encode) throws UnsupportedEncodingException {
        IOContext ctxt = _createContext(_createContentReference(out), false);
        ctxt.setEncoding(encode);
        return _createGenerator(this._createWriter(out, encode), ctxt);
    }

    @Override
    public PHPJsonGenerator createGenerator(Writer writer) {
        IOContext ctxt = _createContext(_createContentReference(writer), false);
        return _createGenerator(writer, ctxt);
    }

    @Override
    public PHPJsonGenerator _createGenerator(Writer out, IOContext ctxt) {
        PHPJsonGenerator generator = new PHPJsonGenerator(0, _objectCodec, out);
        return generator;
    }

    protected Writer _createWriter(OutputStream out, JsonEncoding encode) throws UnsupportedEncodingException {
        return new OutputStreamWriter(out, encode.getJavaName());
    }

    @Override
    public PHPJsonParser createParser(byte[] data) throws UnsupportedEncodingException {
        IOContext ctxt = _createContext(_createContentReference(data), true);
        return this._createParser(data, 0, data.length, ctxt);
    }

    @Override
    public PHPJsonParser createParser(byte[] data, int offset, int len) throws UnsupportedEncodingException {
        IOContext ctxt = _createContext(_createContentReference(data, offset, len), true);
        return this._createParser(data, offset, len, ctxt);
    }

    @Override
    public PHPJsonParser createParser(File f) throws IOException {
        IOContext ctxt = _createContext(_createContentReference(f), true);
        InputStream inputStream = new FileInputStream(f);
        return this._createParser(inputStream, ctxt);
    }

    @Override
    public PHPJsonParser createParser(InputStream in) throws UnsupportedEncodingException {
        IOContext ctxt = _createContext(_createContentReference(in), false);
        return this._createParser(in, ctxt);
    }

    @Override
    public PHPJsonParser createParser(Reader r) {
        IOContext ctxt = _createContext(_createContentReference(r), true);
        return this._createParser(r, ctxt);
    }

    @Override
    public PHPJsonParser createParser(String content){
        return this.createParser(new StringReader(content));
    }

    @Override
    public PHPJsonParser createParser(URL url) throws IOException {
        IOContext ctxt = _createContext(_createContentReference(url), true);
        InputStream inputStream = _optimizedStreamFromURL(url);
        return this._createParser(inputStream, ctxt);
    }

    @Override
    protected PHPJsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws UnsupportedEncodingException {
        JsonEncoding enc = ctxt.getEncoding();
        Reader reader = this._createReader(data, offset, len, enc);
        return new PHPJsonParser(ctxt, reader, _parserFeatures, _objectCodec);
    }

    @Override
    protected PHPJsonParser _createParser(InputStream in, IOContext ctxt) throws UnsupportedEncodingException {
        JsonEncoding enc = ctxt.getEncoding();
        Reader reader = this._createReader(in, enc);
        return new PHPJsonParser(ctxt, reader, _parserFeatures, _objectCodec);
    }

    @Override
    protected PHPJsonParser _createParser(Reader r, IOContext ctxt) {
        return new PHPJsonParser(ctxt, r, _parserFeatures, _objectCodec);
    }

    protected Reader _createReader(byte[] data, int offset, int len, JsonEncoding jsonEncoding) throws UnsupportedEncodingException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data, offset, len);
        return new InputStreamReader(byteArrayInputStream, jsonEncoding.getJavaName());
    }
    protected Reader _createReader(InputStream inputStream, JsonEncoding encoding) throws UnsupportedEncodingException {
        return new InputStreamReader(inputStream, encoding.getJavaName());
    }
}