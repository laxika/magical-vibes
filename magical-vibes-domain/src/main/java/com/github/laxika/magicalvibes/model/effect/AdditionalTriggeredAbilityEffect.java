package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes a triggered ability from another matching permanent under the same
 * player's control trigger one additional time, optionally while a condition is met.
 */
public record AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate, Condition condition)
        implements CardEffect {

    public AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate) {
        this(sourcePredicate, null);
    }
}
