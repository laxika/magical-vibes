package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At least {@code minCount} permanents matching the predicate entered the battlefield
 * under the selected player scope this turn: {@link CountScope#CONTROLLER} the condition's controller,
 * {@link CountScope#ANY_PLAYER} any player, {@link CountScope#TARGET_PLAYER} "that player" of the trigger event
 * (the context's target, else the controller of its triggering permanent: "whenever a land an opponent controls
 * enters, if that player had another land enter the battlefield under their control this turn", Tunnel Ignus).
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
        if (scope != CountScope.CONTROLLER && scope != CountScope.ANY_PLAYER && scope != CountScope.TARGET_PLAYER) {
            throw new IllegalArgumentException("scope must be CONTROLLER, ANY_PLAYER or TARGET_PLAYER");
        }
    }

    @Override
    public String conditionName() {
        return scope == CountScope.ANY_PLAYER ? "permanent entered this turn"
                : scope == CountScope.TARGET_PLAYER ? "permanent entered under that player's control this turn"
                : "permanent entered under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return scope == CountScope.ANY_PLAYER ? "not enough permanents entered the battlefield this turn"
                : scope == CountScope.TARGET_PLAYER
                ? "not enough permanents entered the battlefield under that player's control this turn"
                : "not enough permanents entered the battlefield under your control this turn";
    }
}
