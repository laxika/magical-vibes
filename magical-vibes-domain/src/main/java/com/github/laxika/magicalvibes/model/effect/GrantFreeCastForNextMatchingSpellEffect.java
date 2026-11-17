package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants a one-shot zero-mana alternative cost to the next matching spell cast this turn. As a floating effect on the
 * player it is also what the chosen-card-type and seek grants become as they resolve.
 */
public record GrantFreeCastForNextMatchingSpellEffect(CardPredicate predicate) implements NextMatchingSpellEffect {
}
