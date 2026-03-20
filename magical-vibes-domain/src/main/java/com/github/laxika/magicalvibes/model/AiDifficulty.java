package com.github.laxika.magicalvibes.model;

public enum AiDifficulty {

    EASY("Easy", 800L),
    MEDIUM("Medium", 1200L),
    HARD("Hard", 1500L);

    private final String displayName;
    private final long decisionDelayMs;

    AiDifficulty(String displayName, long decisionDelayMs) {
        this.displayName = displayName;
        this.decisionDelayMs = decisionDelayMs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDecisionDelayMs() {
        return decisionDelayMs;
    }
}
