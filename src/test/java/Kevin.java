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
public class Kevin {
    private String age;
    private String temp;
    private List<String> list;
    private List<String> nullMap;
    private Map<String, Object> nullList;

    @Override
    public String toString() {
        return "age=" + age + ", temp=" + temp + ", list=" + list + ", nullMap=" + nullMap + ", nullList=" + nullList;
    }
}
