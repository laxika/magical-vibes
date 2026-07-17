package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of your library. You may reveal a creature card and/or a land
 * card from among them and put the revealed cards into your hand. Put the rest on the bottom of your
 * library in any order. Unlike {@link LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect}
 * with a "creature or land" predicate, this bounds the pick to <em>at most one creature and at most
 * one land</em> (Gift of the Gargantuan), which is why it runs as two sequential picks rather than a
 * single multi-select. Resolution lives in
 * {@code LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffectHandler}.
 */
public record LookAtTopCardsRevealCreatureAndLandToHandRestOnBottomEffect(int count) implements CardEffect {
}
