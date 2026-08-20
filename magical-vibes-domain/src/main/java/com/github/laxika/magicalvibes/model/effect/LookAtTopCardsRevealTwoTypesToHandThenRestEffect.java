package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.UUID;

/**
 * Look at (or, when {@code reveal}, reveal) the top {@code count} cards of your library. You may put
 * up to one card for each requested type or subtype from among them into {@code chosenDestination},
 * then put the rest to {@code restDestination}. The at-most-one-per-category bound is enforced by running
 * sequential single-card picks over the same looked-at cards, which is why this is its own record
 * rather than a single multi-select
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
 * @param firstType       the first card type offered in the bounded-pick flow
 * @param secondType      the second card type offered in the bounded-pick flow
 * @param subtypePicks   optional subtype categories offered after the first card type categories;
 *                       used by Kaalia, Zenith Seeker
 * @param restDestination where the not-chosen cards go ({@code BOTTOM_OF_LIBRARY} or {@code GRAVEYARD})
 * @param reveal          when true the whole look is public (the looked-at cards are logged)
 * @param chosenDestination where the chosen cards go ({@code HAND} or {@code BATTLEFIELD})
 */
public record LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
        DynamicAmount count,
        CardType firstType,
        CardType secondType,
        List<CardSubtype> subtypePicks,
        LookDestination restDestination,
        boolean reveal,
        LibrarySearchDestination chosenDestination,
        CardPredicate firstPredicate,
        String firstPrompt,
        CardPredicate secondPredicate,
        String secondPrompt,
        boolean randomRest,
        LibraryScope scope,
        UUID playerId) implements CardEffect {

    public LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
            DynamicAmount count, CardType firstType, CardType secondType,
            List<CardSubtype> subtypePicks, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination) {
        this(count, firstType, secondType, subtypePicks, restDestination, reveal,
                chosenDestination, null, null, null, null, false,
                LibraryScope.CONTROLLER, null);
    }

    public LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
            int count, CardType firstType, CardType secondType, List<CardSubtype> subtypePicks,
            LookDestination restDestination, boolean reveal) {
        this(new Fixed(count), firstType, secondType, subtypePicks, restDestination, reveal,
                LibrarySearchDestination.HAND);
    }

    public LookAtTopCardsRevealTwoTypesToHandThenRestEffect {
        subtypePicks = List.copyOf(subtypePicks);
        if (chosenDestination != LibrarySearchDestination.HAND
                && chosenDestination != LibrarySearchDestination.BATTLEFIELD) {
            throw new IllegalArgumentException("Chosen destination must be hand or battlefield");
        }
    }

    /** Gift of the Gargantuan: look privately, may take a creature and/or a land, rest on the bottom. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect creatureAndLandToHandRestOnBottom(int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                new Fixed(count), CardType.CREATURE, CardType.LAND, List.of(),
                LookDestination.BOTTOM_OF_LIBRARY, false, LibrarySearchDestination.HAND);
    }

    /** Benefaction of Rhonas: reveal publicly, may take a creature and/or an enchantment, rest to graveyard. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect creatureAndEnchantmentToHandRestToGraveyard(int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                new Fixed(count), CardType.CREATURE, CardType.ENCHANTMENT, List.of(),
                LookDestination.GRAVEYARD, true, LibrarySearchDestination.HAND);
    }

    /** Kaalia, Zenith Seeker: privately look at six and may take one Angel, Demon, and Dragon. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect angelDemonDragonToHandRestOnBottom(int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                new Fixed(count), null, null,
                List.of(CardSubtype.ANGEL, CardSubtype.DEMON, CardSubtype.DRAGON),
                LookDestination.BOTTOM_OF_LIBRARY, false, LibrarySearchDestination.HAND);
    }

    /** Green Sun's Twilight: reveal, then put the chosen creature and/or land onto the battlefield. */
    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect creatureAndLandToBattlefieldRestOnBottomRandom(
            DynamicAmount count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                count, CardType.CREATURE, CardType.LAND, List.of(),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true, LibrarySearchDestination.BATTLEFIELD);
    }

    public static LookAtTopCardsRevealTwoTypesToHandThenRestEffect landAndInstantOrSorceryToHandRestOnBottomRandom(
            int count) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                new Fixed(count), null, null, List.of(), LookDestination.BOTTOM_OF_LIBRARY, false,
                LibrarySearchDestination.HAND,
                new CardTypePredicate(CardType.LAND), "a land card",
                new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                "an instant or sorcery card", true, LibraryScope.EACH_PLAYER, null);
    }

    public LookAtTopCardsRevealTwoTypesToHandThenRestEffect forPlayer(UUID playerId) {
        return new LookAtTopCardsRevealTwoTypesToHandThenRestEffect(
                count, firstType, secondType, subtypePicks, restDestination, reveal,
                chosenDestination, firstPredicate, firstPrompt, secondPredicate, secondPrompt,
                randomRest, LibraryScope.CONTROLLER, playerId);
    }
}
