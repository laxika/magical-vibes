package com.github.laxika.magicalvibes.model.condition;

/** The spell was cast from anywhere other than the controller's hand (e.g. flashback). */
public record CastNotFromHand() implements Condition {

    @Override
    public String conditionName() {
        return "cast from anywhere other than your hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was cast from hand";
    }
}
