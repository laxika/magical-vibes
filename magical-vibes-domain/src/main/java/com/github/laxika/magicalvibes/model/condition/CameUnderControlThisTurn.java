package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent came under its controller's control this turn — i.e. it is still
 * summoning sick at the point the condition is evaluated (summoning sickness is only cleared
 * during that player's untap step). Used by Erg Raiders' "unless it came under your control
 * this turn".
 */
public record CameUnderControlThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "came under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it has been under your control since your last turn";
    }
}
