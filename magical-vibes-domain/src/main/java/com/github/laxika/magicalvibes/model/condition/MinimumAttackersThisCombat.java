package com.github.laxika.magicalvibes.model.condition;

/**
 * At least {@code minimumAttackers} creatures were declared as attackers this combat, counting
 * those that have since left the battlefield or combat. Unlike {@link MinimumAttackers}, it is read
 * from the combat's declared attackers rather than a trigger-time snapshot, so an "at end of combat,
 * if … attacked this combat" trigger can check it (Kytheon, Hero of Akros).
 */
public record MinimumAttackersThisCombat(int minimumAttackers) implements Condition {

    @Override
    public String conditionName() {
        return minimumAttackers + " or more creatures attacked this combat";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimumAttackers + " creatures attacked this combat";
    }
}
