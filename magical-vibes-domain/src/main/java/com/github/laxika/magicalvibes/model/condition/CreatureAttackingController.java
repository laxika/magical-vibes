package com.github.laxika.magicalvibes.model.condition;

/**
 * A creature is attacking the controller (the player themselves, not a planeswalker they
 * control) — Qasali Ambusher's "if a creature is attacking you". Evaluated against the current
 * declared attackers.
 */
public record CreatureAttackingController() implements Condition {

    @Override
    public String conditionName() {
        return "a creature is attacking you";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature is attacking you";
    }
}
