package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least {@code minCount} permanents matching the predicate entered the battlefield
 * under the selected player scope this turn.
 */
public record PermanentEnteredThisTurn(CardPredicate predicate, int minCount, CountScope scope)
        implements Condition {

    public PermanentEnteredThisTurn(CardPredicate predicate, int minCount) {
        this(predicate, minCount, CountScope.CONTROLLER);
    }

    public PermanentEnteredThisTurn {
        if (minCount < 1) {
            throw new IllegalArgumentException("minCount must be positive");
        }
        if (scope != CountScope.CONTROLLER && scope != CountScope.ANY_PLAYER) {
            throw new IllegalArgumentException("scope must be CONTROLLER or ANY_PLAYER");
        }
    }

    @Override
    public String conditionName() {
        return scope == CountScope.ANY_PLAYER
                ? "permanent entered this turn"
                : "permanent entered under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return scope == CountScope.ANY_PLAYER
                ? "not enough permanents entered the battlefield this turn"
                : "not enough permanents entered the battlefield under your control this turn";
    }
}
