package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Moves the spell that caused this triggered ability to the bottom of its owner's library, then
 * reveals until a card matching {@code predicate} is found and offers that card for a free cast.
 */
public record PutTriggeringSpellOnBottomThenRevealEffect(CardPredicate predicate)
        implements TriggeringSpellReferencingEffect {
}
