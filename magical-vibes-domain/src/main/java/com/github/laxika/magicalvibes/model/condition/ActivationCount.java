package com.github.laxika.magicalvibes.model.condition;

/**
 * The activated ability at {@code abilityIndex} on the source permanent has been
 * activated at least {@code threshold} times this turn (e.g. Dragon Whelp).
 */
public record ActivationCount(int threshold, int abilityIndex) implements Condition {

    @Override
    public String conditionName() {
        return "activated " + threshold + "+ times";
    }

    @Override
    public String conditionNotMetReason() {
        return "ability has been activated fewer than " + threshold + " times this turn";
    }
}
