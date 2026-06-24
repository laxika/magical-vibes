package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: cards matching {@code filter} in graveyards and libraries can't enter the
 * battlefield. This is a continuous restriction that stops reanimation (return a matching card
 * from a graveyard to the battlefield), undying/persist returns, and effects that put matching
 * cards from libraries onto the battlefield. The blocked card stays in its current zone.
 * <p>
 * A {@code null} filter matches every card. Does not affect cards entering from the hand, exile,
 * or the stack, and does not affect tokens (which are not cards).
 * <p>
 * Used by Grafdigger's Cage (DKA), which supplies a {@code CardTypePredicate(CREATURE)} filter so
 * only creature cards are blocked.
 */
public record CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect(CardPredicate filter) implements CardEffect {
}
