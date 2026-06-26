package com.gaurav.h1bchecker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class H1bResponse {

    private String company;
    private Boolean sponsors;
    private Integer totalPetitions;
    private Integer recentYear;
    private Integer avgSalary;
    private String topRole;
    private String summary;
    private String confidence;
    private String h1bDataUrl;
    private String error;

    // Constructors
    public H1bResponse() {}

    public static H1bResponse error(String company, String message) {
        H1bResponse r = new H1bResponse();
        r.company = company;
        r.error = message;
        return r;
    }

    // Getters and setters
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Boolean getSponsors() { return sponsors; }
    public void setSponsors(Boolean sponsors) { this.sponsors = sponsors; }

    public Integer getTotalPetitions() { return totalPetitions; }
    public void setTotalPetitions(Integer totalPetitions) { this.totalPetitions = totalPetitions; }

    public Integer getRecentYear() { return recentYear; }
    public void setRecentYear(Integer recentYear) { this.recentYear = recentYear; }

    public Integer getAvgSalary() { return avgSalary; }
    public void setAvgSalary(Integer avgSalary) { this.avgSalary = avgSalary; }

    public String getTopRole() { return topRole; }
    public void setTopRole(String topRole) { this.topRole = topRole; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getH1bDataUrl() { return h1bDataUrl; }
    public void setH1bDataUrl(String h1bDataUrl) { this.h1bDataUrl = h1bDataUrl; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
