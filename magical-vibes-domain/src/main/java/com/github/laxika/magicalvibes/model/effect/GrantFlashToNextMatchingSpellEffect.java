package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The next spell its controller casts that matches {@code predicate} can be cast as though it had flash. It is what
 * {@link GrantFlashToNextSpellOfTypeThisTurnEffect} (Quicken) and
 * {@link GrantFlashToNextSpellOfChosenSubtypeThisTurnEffect} become as they resolve: a floating effect on the player
 * until a matching spell is cast.
 */
public record GrantFlashToNextMatchingSpellEffect(CardPredicate predicate) implements NextMatchingSpellEffect {
}
