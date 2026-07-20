package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Look at (or, when {@code reveal}, reveal) the top {@code count} cards of your library. You may put
 * up to one {@code firstType} card and up to one {@code secondType} card from among them into your
 * hand, then put the rest to {@code restDestination}. The at-most-one-per-type bound is enforced by
 * running two sequential single-card picks over the same looked-at cards (first type, then second
 * type), which is why this is its own record rather than a single multi-select
 * {@link LookAtTopCardsEffect}. Resolution lives in
 * {@code LookAtTopCardsRevealTwoTypesToHandThenRestEffectHandler}.
 *
 * <ul>
 *   <li>{@link #creatureAndLandToHandRestOnBottom(int)} — look privately, may take a creature and/or
 *       a land, rest on the bottom of the library (Gift of the Gargantuan, count 4).</li>
 *   <li>{@link #creatureAndEnchantmentToHandRestToGraveyard(int)} — reveal publicly, may take a
 *       creature and/or an enchantment, rest into the graveyard (Benefaction of Rhonas, count 5).</li>
 * </ul>
 *
 * @param count           how many cards to look at / reveal from the top of the library
 * @param firstType       the card type offered in the first pick
 * @param secondType      the card type offered in the second pick
 * @param restDestination where the not-chosen cards go ({@code BOTTOM_OF_LIBRARY} or {@code GRAVEYARD})
 * @param reveal          when true the whole look is public (the looked-at cards are logged)
 */
public record LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
        int count,
        CardType firstType,
        CardType secondType,
        LookDestination restDestination,
        boolean reveal) implements CardEffect {

    /** Gift of the Gargantuan: look privately, may take a creature and/or a land, rest on the bottom. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect creatureAndLandToHandRestOnBottom(int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                count, CardType.CREATURE, CardType.LAND, LookDestination.BOTTOM_OF_LIBRARY, false);
    }

    /** Benefaction of Rhonas: reveal publicly, may take a creature and/or an enchantment, rest to graveyard. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect creatureAndEnchantmentToHandRestToGraveyard(int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                count, CardType.CREATURE, CardType.ENCHANTMENT, LookDestination.GRAVEYARD, true);
    }
}
