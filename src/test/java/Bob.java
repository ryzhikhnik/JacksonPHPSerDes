import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bob {
    private String name;
    private int age;
    private double temp;
    private Map<String, Object> map;
    private Map<String, Object> nullMap;
    private List<String> list;
    private List<String> nullList;

    public String toString() {
        return "name=" + name + ", age=" + age + ", temp=" + temp + ", map=" + map + ", nullMap=" + nullMap + ", list=" + list + ", nullList=" + nullList;
    }
}
