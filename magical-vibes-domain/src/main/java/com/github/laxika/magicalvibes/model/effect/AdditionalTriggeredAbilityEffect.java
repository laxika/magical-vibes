package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes a triggered ability from another matching permanent under the same
 * player's control trigger one additional time.
 */
public record AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate) implements CardEffect {
}
