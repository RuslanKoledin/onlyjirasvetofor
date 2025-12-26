package incuat.kg.svetoofor.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель инцидента из JIRA
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    private String id;
    private String key;
    private JiraFields fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JiraFields getFields() {
        return fields;
    }

    public void setFields(JiraFields fields) {
        this.fields = fields;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraFields {
        private String summary;

        @JsonProperty("creator")
        private JiraUser author;

        private JiraStatus status;
        private JiraPriority priority;
        private JiraIssueType issuetype;
        private String created;

        @JsonProperty("resolutiondate")
        private String resolutionDate;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public JiraUser getAuthor() {
            return author;
        }

        public void setAuthor(JiraUser author) {
            this.author = author;
        }

        public JiraStatus getStatus() {
            return status;
        }

        public void setStatus(JiraStatus status) {
            this.status = status;
        }

        public JiraPriority getPriority() {
            return priority;
        }

        public void setPriority(JiraPriority priority) {
            this.priority = priority;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getResolutionDate() {
            return resolutionDate;
        }

        public void setResolutionDate(String resolutionDate) {
            this.resolutionDate = resolutionDate;
        }

        public JiraIssueType getIssuetype() {
            return issuetype;
        }

        public void setIssuetype(JiraIssueType issuetype) {
            this.issuetype = issuetype;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraUser {
        private String displayName;
        private String emailAddress;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraStatus {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraPriority {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JiraIssueType {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Override
    public String toString() {
        return "JiraIssue{" +
                "key='" + key + '\'' +
                ", summary='" + (fields != null ? fields.getSummary() : null) + '\'' +
                ", status='" + (fields != null && fields.getStatus() != null ? fields.getStatus().getName() : null) + '\'' +
                ", priority='" + (fields != null && fields.getPriority() != null ? fields.getPriority().getName() : null) + '\'' +
                '}';
    }
}
