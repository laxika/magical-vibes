package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * On resolution, the controller may pay {@code X}. If they do, they choose a matching creature
 * card with mana value X in their graveyard and return it to the battlefield immediately.
 *
 * <p>The target is chosen during the same resolution after X is paid. This differs from
 * {@link PayXManaReanimateCreatureWithManaValueXEffect}, whose wording creates a reflexive trigger
 * that players can respond to after the payment.</p>
 *
 * @param filter additional card restriction, such as a creature subtype or excluding the source
 */
public record PayXManaReturnTargetCreatureWithManaValueXEffect(CardPredicate filter) implements CardEffect {
}
