import PHPSerDes.PHPJsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.*;

public class TestMain {
    ObjectMapper customObjectMapper = new ObjectMapper(new PHPJsonFactory());
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void stringSimpleTest() throws JsonProcessingException {
        String json = "{\"key-привет1\":\"str1\",\"key2-\uD83C\uDF0D\":123,\"key3\":123.456,\"key4\":true,\"key5\":false,\"key6\":null,\"key7\":[\"str2\",123,123.456,true,false,null,{},[],{\"kkk\":\"vvv\",\"123\":\"111\",\"-123\":\"999\",\"-\":\"999\",\"\":\"999\"}],\"key8\":{}}";
        System.out.println("JSON = " + json);

        Map<String, Object> mapJson = objectMapper.readValue(json, LinkedHashMap.class);
        String jsonPhp = customObjectMapper.writeValueAsString(mapJson);
        System.out.println("PHP = " + jsonPhp);
        Map<String, Object> mapJsonPhpJson = customObjectMapper.readValue(jsonPhp, LinkedHashMap.class);
        String jsonPhpJson = objectMapper.writeValueAsString(mapJsonPhpJson);
        System.out.println("JSON = " + jsonPhpJson);
    }

    @Test
    public void objectSimpleTest() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("first", 123);

        Map<String, Object> nullMap = new HashMap<>();

        List<String> list = new ArrayList<>();
        list.add("asd");
        list.add("qwerty");

        List<String> nullList = new ArrayList<>();

        Bob bob = new Bob("Bob", 18, 36.8, map, nullMap, list, nullList);
        String rawJsonBob = customObjectMapper.writeValueAsString(bob);
        System.out.println(rawJsonBob);

        Map<String, Object> nullList1 = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        list1.add("none");
        list1.add("cheburek");

        List<String> nullMap1 = new ArrayList<>();

        Kevin kevin = new Kevin("24", "37.9", list1, nullMap1, nullList1);
        String rawJsonKevin = customObjectMapper.writeValueAsString(kevin);
        System.out.println(rawJsonKevin);

        Bob bobViaKevinJson = customObjectMapper.readValue(rawJsonKevin, Bob.class);
        System.out.println(bobViaKevinJson);

        Kevin kevinViaBobJson = customObjectMapper.readValue(rawJsonBob, Kevin.class);
        System.out.println(kevinViaBobJson);
    }
}