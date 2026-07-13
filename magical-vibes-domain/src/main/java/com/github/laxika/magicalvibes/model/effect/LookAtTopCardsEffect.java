package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at (or reveal) the top {@code lookCount} cards of your library, choose up to
 * {@code chooseCount} of them (optionally restricted to {@code choosePredicate}) to put into your
 * hand, and put the rest to {@code restDestination}. Collapses the "look at top N, put some into
 * hand" family:
 *
 * <ul>
 *   <li>{@link #chooseOneToHandRestOnBottom(DynamicAmount)} — one to hand, rest on the bottom of
 *       the library (Stress Dream {@code Fixed(2)}; Shrine of Piercing Vision
 *       {@code CountersOnSource(CHARGE)}; Jar of Eyeballs {@code XValue()} from its remove-counters
 *       cost).</li>
 *   <li>{@link #chooseNToHandRestToGraveyard(int, int, CardPredicate, boolean)} — up to N to hand,
 *       rest into the graveyard, optionally filtered / revealed (Forbidden Alchemy, Dark Bargain,
 *       Tower Geist, Tracker's Instincts).</li>
 *   <li>{@link #chooseOneToHandRestToExile(DynamicAmount)} — one to hand, exile the rest (Browse).</li>
 * </ul>
 *
 * <p>The chosen cards always go to the hand; a {@code chosenDestination} field is intentionally not
 * added yet (no folded card varies it — battlefield/graveyard "choose" look effects remain their
 * own records for now).
 *
 * @param lookCount       how many cards to look at / reveal from the top of the library
 * @param chooseCount     the maximum number to put into hand
 * @param choosePredicate when non-null, only matching cards are eligible for hand
 * @param restDestination where the not-chosen cards go ({@code BOTTOM_OF_LIBRARY} or {@code GRAVEYARD})
 * @param reveal          when true the cards are revealed publicly rather than looked at privately
 */
public record LookAtTopCardsEffect(
        DynamicAmount lookCount,
        DynamicAmount chooseCount,
        CardPredicate choosePredicate,
        LookDestination restDestination,
        boolean reveal
) implements CardEffect {

    /** One card to hand, the rest on the bottom of the library. */
    public static LookAtTopCardsEffect chooseOneToHandRestOnBottom(DynamicAmount lookCount) {
        return new LookAtTopCardsEffect(lookCount, new Fixed(1), null,
                LookDestination.BOTTOM_OF_LIBRARY, false);
    }

    /** One card to hand, exile the rest (Browse). */
    public static LookAtTopCardsEffect chooseOneToHandRestToExile(DynamicAmount lookCount) {
        return new LookAtTopCardsEffect(lookCount, new Fixed(1), null,
                LookDestination.EXILE, false);
    }

    /** Up to {@code chooseCount} cards to hand, the rest into the graveyard. */
    public static LookAtTopCardsEffect chooseNToHandRestToGraveyard(int lookCount, int chooseCount) {
        return chooseNToHandRestToGraveyard(lookCount, chooseCount, null, false);
    }

    /** Up to {@code chooseCount} matching cards to hand (optionally revealed), the rest into the graveyard. */
    public static LookAtTopCardsEffect chooseNToHandRestToGraveyard(
            int lookCount, int chooseCount, CardPredicate choosePredicate, boolean reveal) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(chooseCount), choosePredicate,
                LookDestination.GRAVEYARD, reveal);
    }
}
