package incuat.kg.svetoofor.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Результат поиска инцидентов в JIRA
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSearchResult {

    private int startAt;
    private int maxResults;
    private int total;
    private List<JiraIssue> issues;

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<JiraIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<JiraIssue> issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "JiraSearchResult{" +
                "total=" + total +
                ", issues=" + (issues != null ? issues.size() : 0) +
                '}';
    }
}
