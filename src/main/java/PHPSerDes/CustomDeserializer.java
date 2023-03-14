package PHPSerDes;

import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.Set;

public class CustomDeserializer extends PHPJsonParser {
    private final static char SEMICOLON = ';';

    private static class ItemFields {
        int COUNT_KEY;
        boolean IS_MAIN_ARRAY;
        JsonToken TYPE_FOR_JSON_TOKEN;
        String VALUE;

        List<Map<ItemFields, ItemFields>> nestedItemFields;
    }

    private final Reader _reader;
    private final List<JsonToken> _tokenList;
    private final List<String> _currentValueList;
    private final Stack<ItemFields> _rawRes;

    CustomDeserializer(Reader _reader, List<JsonToken> _tokenList, List<String> _currentValueList) {
        this._reader = _reader;
        this._tokenList = _tokenList;
        this._currentValueList = _currentValueList;

        _rawRes = new Stack<>();
    }

    protected void deserialize() throws IOException {
        while (true) {
            int nextToken = this._reader.read();

            if (nextToken == -1) {
                break;
            } else if (nextToken == '}') {
                continue;
            }

            switch ((char) nextToken) {
                case 'i': {
                    String intValue = readNextTokens();

                    ItemFields itemFields = new ItemFields();
                    itemFields.VALUE = intValue;
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.VALUE_NUMBER_INT;

                    if (_rawRes.empty()) {
                        formatterResult(itemFields);
                    } else {
                        addItemToRawRes(itemFields, 'i');
                    }
                    break;
                }
                case 'd': {
                    String doubleValue = readNextTokens();

                    ItemFields itemFields = new ItemFields();
                    itemFields.VALUE = doubleValue;
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.VALUE_NUMBER_FLOAT;

                    if (_rawRes.empty()) {
                        formatterResult(itemFields);
                    } else {
                        addItemToRawRes(itemFields, 'd');
                    }
                    break;
                }
                case 'b': {
                    String booleanValue = readNextTokens();
                    JsonToken boolToken;

                    if (booleanValue.equals("1")) {
                        booleanValue = "true";
                        boolToken = JsonToken.VALUE_TRUE;
                    } else {
                        booleanValue = "false";
                        boolToken = JsonToken.VALUE_FALSE;
                    }

                    ItemFields itemFields = new ItemFields();
                    itemFields.VALUE = booleanValue;
                    itemFields.TYPE_FOR_JSON_TOKEN = boolToken;

                    if (_rawRes.empty()) {
                        formatterResult(itemFields);
                    } else {
                        addItemToRawRes(itemFields, 'b');
                    }
                    break;
                }
                case 'N': {
                    ItemFields itemFields = new ItemFields();
                    itemFields.VALUE = "null";
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.VALUE_NULL;

                    nextToken = this._reader.read();
                    if (nextToken != SEMICOLON) {
                        _reportUnexpectedChar(nextToken, SEMICOLON);
                    }

                    if (_rawRes.empty()) {
                        formatterResult(itemFields);
                    } else {
                        addItemToRawRes(itemFields, 'N');
                    }
                    break;
                }
                case 's': {
                    int byteCount = checkLength();
                    nextToken = this._reader.read();

                    if (nextToken != INT_QUOTE) {
                        _reportUnexpectedChar(nextToken, INT_QUOTE);
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    while (byteCount > 0) {
                        nextToken = this._reader.read();
                        if ((nextToken >= 0x0001) && (nextToken <= 0x007F)) {
                            byteCount--;
                        } else if (nextToken > 0x07FF) {
                            byteCount -= 3;
                        } else {
                            byteCount -= 2;
                        }
                        stringBuilder.append((char) nextToken);
                    }
                    nextToken = this._reader.read();
                    if (nextToken != INT_QUOTE) {
                        _reportUnexpectedChar(nextToken, INT_QUOTE);
                    }
                    nextToken = this._reader.read();
                    if (nextToken != SEMICOLON) {
                        _reportUnexpectedChar(nextToken, SEMICOLON);
                    }

                    ItemFields itemFields = new ItemFields();
                    itemFields.VALUE = String.valueOf(stringBuilder);
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.VALUE_STRING;

                    if (_rawRes.empty()) {
                        formatterResult(itemFields);
                    } else {
                        addItemToRawRes(itemFields, 's');
                    }
                    break;
                }
                case 'a': {
                    int count = checkLength();

                    ItemFields itemFields = new ItemFields();
                    itemFields.COUNT_KEY = count;
                    itemFields.IS_MAIN_ARRAY = _rawRes.empty();
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.START_OBJECT;
                    itemFields.nestedItemFields = new ArrayList<>();

                    _rawRes.push(itemFields);
                    nextToken = this._reader.read();
                    if (nextToken != -1 && nextToken != INT_LCURLY) {
                        _reportUnexpectedChar(nextToken, INT_LCURLY);
                    }
                    if (count == 0) {
                        checkBuild();
                    }
                    break;
                }
                case 'O': {
                    int objNameLength = checkLength();
                    nextToken = this._reader.read();

                    if (nextToken != INT_QUOTE) {
                        _reportUnexpectedChar(nextToken, INT_QUOTE);
                    }

                    while (objNameLength > 0) {
                        nextToken = this._reader.read();
                        if ((nextToken >= 0x0001) && (nextToken <= 0x007F)) {
                            objNameLength--;
                        } else if (nextToken > 0x07FF) {
                            objNameLength -= 3;
                        } else {
                            objNameLength -= 2;
                        }
                    }

                    nextToken = this._reader.read();
                    if (nextToken != INT_QUOTE) {
                        _reportUnexpectedChar(nextToken, INT_QUOTE);
                    }

                    int ojbCount = checkLength();

                    ItemFields itemFields = new ItemFields();
                    itemFields.COUNT_KEY = ojbCount;
                    itemFields.IS_MAIN_ARRAY = _rawRes.empty();
                    itemFields.TYPE_FOR_JSON_TOKEN = JsonToken.START_OBJECT;
                    itemFields.nestedItemFields = new ArrayList<>();

                    _rawRes.push(itemFields);
                    nextToken = this._reader.read();

                    if (nextToken != -1 && nextToken != INT_LCURLY) {
                        _reportUnexpectedChar(nextToken, INT_LCURLY);
                    }
                    if (ojbCount == 0) {
                        checkBuild();
                    }
                    break;
                }
                default: {
                    _reportError("Not supported type '" + (char) nextToken + "', or this string is not php serializable format");
                    break;
                }
            }

            if (!_rawRes.empty() && (_rawRes.peek().TYPE_FOR_JSON_TOKEN == JsonToken.START_OBJECT)) {
                int objSize = _rawRes.peek().COUNT_KEY;
                if (objSize == 0) {
                    ItemFields lastItem = _rawRes.pop();
                    if (checkIsArray(lastItem)) {
                        lastItem.TYPE_FOR_JSON_TOKEN = JsonToken.START_ARRAY;
                    }
                    if (_rawRes.empty()) {
                        formatterResult(lastItem);
                    }
                }
            }
        }
    }

    private void formatterResult(ItemFields result) {
        JsonToken jsonToken = null;
        if (result.TYPE_FOR_JSON_TOKEN != null) {
            jsonToken = result.TYPE_FOR_JSON_TOKEN;
        }
        if (jsonToken == JsonToken.START_ARRAY) {
            _tokenList.add(jsonToken);
            for (Map<ItemFields, ItemFields> dynamicMap : result.nestedItemFields) {
                for (Map.Entry<ItemFields, ItemFields> dynamicEntry : dynamicMap.entrySet()) {
                    ItemFields value = dynamicEntry.getValue();
                    if (value.TYPE_FOR_JSON_TOKEN != JsonToken.START_OBJECT
                            && value.TYPE_FOR_JSON_TOKEN != JsonToken.START_ARRAY) {
                        _tokenList.add(value.TYPE_FOR_JSON_TOKEN);
                        _currentValueList.add(value.VALUE);
                    } else {
                        formatterResult(value);
                    }
                }
            }
            _tokenList.add(JsonToken.END_ARRAY);
        } else if (jsonToken == JsonToken.START_OBJECT) {
            _tokenList.add(jsonToken);
            for (Map<ItemFields, ItemFields> dynamicMap : result.nestedItemFields) {
                for (Map.Entry<ItemFields, ItemFields> dynamicEntry : dynamicMap.entrySet()) {
                    ItemFields key = dynamicEntry.getKey();
                    _tokenList.add(JsonToken.FIELD_NAME);
                    _currentValueList.add(key.VALUE);

                    ItemFields value = dynamicEntry.getValue();
                    if (value.TYPE_FOR_JSON_TOKEN != JsonToken.START_OBJECT
                            && value.TYPE_FOR_JSON_TOKEN != JsonToken.START_ARRAY) {
                        _tokenList.add(value.TYPE_FOR_JSON_TOKEN);
                        _currentValueList.add(value.VALUE);
                    } else {
                        formatterResult(value);
                    }
                }
            }
            _tokenList.add(JsonToken.END_OBJECT);
        } else {
            _tokenList.add(jsonToken);
            _currentValueList.add(result.VALUE);
        }
    }

    private void addItemToRawRes(ItemFields item, char type) {
        ItemFields lastElement = _rawRes.peek();

        if ((type == 's' || type == 'i') && lastElement.TYPE_FOR_JSON_TOKEN == JsonToken.START_OBJECT) {
            _rawRes.push(item);
        } else {
            ItemFields key = _rawRes.pop();
            ItemFields parentItemFields = _rawRes.peek();

            Map<ItemFields, ItemFields> newNested = new HashMap<>();
            newNested.put(key, item);

            parentItemFields.nestedItemFields.add(newNested);
            int count = parentItemFields.COUNT_KEY;
            parentItemFields.COUNT_KEY = count - 1;
            checkBuild();
        }
    }

    private void checkBuild() {
        ItemFields itemFields = _rawRes.peek();
        boolean isMainArray = itemFields.IS_MAIN_ARRAY;
        int count = itemFields.COUNT_KEY;

        if (count == 0 && !isMainArray) {
            ItemFields slaveObj = _rawRes.pop();

            if (checkIsArray(slaveObj)) {
                slaveObj.TYPE_FOR_JSON_TOKEN = JsonToken.START_ARRAY;
            }
            ItemFields objKey = _rawRes.pop();
            itemFields = _rawRes.peek();
            count = itemFields.COUNT_KEY;

            Map<ItemFields, ItemFields> newNested = new HashMap<>();
            newNested.put(objKey, slaveObj);

            itemFields.nestedItemFields.add(newNested);
            itemFields.COUNT_KEY = count - 1;

            checkBuild();
        }
    }

    private boolean checkIsArray(ItemFields itemFields) {
        int iterate = 0;

        if (itemFields.nestedItemFields.size() == 0) {
            return false;
        }

        for (Map<ItemFields, ItemFields> dynamicMap : itemFields.nestedItemFields) {
            Set<ItemFields> keys = dynamicMap.keySet();
            for (ItemFields key : keys) {
                String insideKey = key.VALUE;
                if (insideKey.length() == 0) {
                    return false;
                }
                for (char c : insideKey.toCharArray()) {
                    if (!Character.isDigit(c)) {
                        return false;
                    }
                }
                long intKey = Long.parseLong(insideKey);
                if (intKey != iterate++) {
                    return false;
                }
            }
        }
        return true;
    }

    private int checkLength() throws IOException {
        int nextToken = this._reader.read();
        if (nextToken != INT_COLON) {
            _reportUnexpectedChar(nextToken, INT_COLON);
        }

        StringBuilder stringBuilder = new StringBuilder();
        nextToken = this._reader.read();

        while (nextToken != -1 && nextToken != INT_COLON) {
            stringBuilder.append((char) nextToken);
            nextToken = this._reader.read();
        }
        return Integer.parseInt(String.valueOf(stringBuilder));
    }

    private String readNextTokens() throws IOException {
        int nextToken = this._reader.read();
        if (nextToken != INT_COLON) {
            _reportUnexpectedChar(nextToken, INT_COLON);
        }

        StringBuilder stringBuilder = new StringBuilder();
        nextToken = this._reader.read();
        while (nextToken != -1 && nextToken != SEMICOLON) {
            stringBuilder.append((char) nextToken);
            nextToken = this._reader.read();
        }
        return String.valueOf(stringBuilder);
    }
}