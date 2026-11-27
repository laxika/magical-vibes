package com.github.laxika.magicalvibes.model.condition;

/**
 * Whether the attacking creature's target is an opponent rather than a permanent; as the event condition of a combat
 * damage trigger ("deals combat damage to an opponent"), whether the player dealt the damage is an opponent, which
 * redirected damage (CR 614.9) may make the ability's controller instead.
 */
public record AttackedTargetIsOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "attacked target is an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "attacked target is not an opponent";
    }
}
