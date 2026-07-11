package com.github.laxika.magicalvibes.model.condition;

/**
 * At least {@code minimumAttackers} creatures are attacking. The attacker count is
 * snapshotted onto the stack entry's {@code xValue} at trigger time.
 */
public record MinimumAttackers(int minimumAttackers) implements Condition {

    @Override
    public String conditionName() {
        return minimumAttackers + " or more attackers";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimumAttackers + " creatures attacking";
    }
}
