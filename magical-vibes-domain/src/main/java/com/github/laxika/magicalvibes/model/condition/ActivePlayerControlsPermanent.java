package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The active player (the player whose turn/step it is) controls at least one permanent matching
 * the predicate. Used as the intervening-"if" for each-player-upkeep triggers that gate on the
 * board of whoever's upkeep it is — "if that player controls a nonblack, nonland permanent"
 * (Urborg Stalker).
 */
public record ActivePlayerControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "active player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "the player does not control a matching permanent";
    }
}
