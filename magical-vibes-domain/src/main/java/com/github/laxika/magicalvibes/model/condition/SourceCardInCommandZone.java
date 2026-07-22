package com.github.laxika.magicalvibes.model.condition;

/**
 * Intervening-if for Eminence abilities that triggered from the command zone:
 * the source card object is still in its controller's command zone.
 * Fails if the card left the command zone (including when cast onto the stack / battlefield),
 * so a BF→command zone move cannot satisfy a battlefield-sourced trigger via this condition.
 */
public record SourceCardInCommandZone() implements Condition {

    @Override
    public String conditionName() {
        return "source in command zone";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not in the command zone";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
