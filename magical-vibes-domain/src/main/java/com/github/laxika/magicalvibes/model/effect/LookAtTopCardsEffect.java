package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at (or reveal) the top {@code lookCount} cards of your library, choose up to
 * {@code chooseCount} of them (optionally restricted to {@code choosePredicate}) to put to
 * {@code chosenDestination}, and put the rest to {@code restDestination}. Collapses the "look at
 * top N, choose some" family:
 *
 * <ul>
 *   <li>{@link #chooseOneToHandRestOnBottom(DynamicAmount)} — one to hand, rest on the bottom of
 *       the library (Stress Dream {@code Fixed(2)}; Shrine of Piercing Vision
 *       {@code CountersOnSource(CHARGE)}; Jar of Eyeballs {@code XValue()} from its remove-counters
 *       cost).</li>
 *   <li>{@link #chooseNToHandRestOnBottomRandom(DynamicAmount, int)} — N to hand, rest on the
 *       bottom in a random order (Memory Deluge: look = {@code ManaSpentToCast()}, choose = 2).</li>
 *   <li>{@link #chooseNToHandRestToGraveyard(int, int, CardPredicate, boolean)} — up to N to hand,
 *       rest into the graveyard, optionally filtered / revealed (Forbidden Alchemy, Dark Bargain,
 *       Tower Geist, Tracker's Instincts). With {@code chooseCount == lookCount} this is also the
 *       choice-free "reveal N, put all matching into your hand, rest into your graveyard" shape
 *       (Mulch) — every eligible card auto-moves to hand.</li>
 *   <li>{@link #chooseOneToHandRestOnTop(int)} — one to hand, the rest back on top of the library
 *       in any order (Diabolic Vision).</li>
 *   <li>{@link #chooseOneToHandRestToExile(DynamicAmount)} — one to hand, exile the rest (Browse).</li>
 *   <li>{@link #mayRevealOneToHandRestOnBottom(int, CardPredicate)} /
 *       {@link #mayRevealAnyNumberToHandRestOnBottom(int, CardPredicate)} /
 *       {@link #mayRevealUpToToHandRestOnBottom(int, CardPredicate, int)} — optional ("may")
 *       reveal-matching-to-hand picks, rest on the bottom (Commune with Nature, Lead the Stampede,
 *       Follow the Lumarets, ...). The look is private; the chosen cards are revealed.</li>
 *   <li>{@link #mayRevealOneToHandRestToGraveyard(int, CardPredicate)} — optional public reveal of
 *       the top N, you may put one matching card into your hand, rest into the graveyard
 *       (Grisly Salvage), or {@link #mayRevealUpToToHandRestToGraveyard(int, CardPredicate,
 *       DynamicAmount)} for a rider that only changes how many are kept (Gather the Pack).</li>
 *   <li>{@link #mayPutMatchingOntoBattlefield(int, CardPredicate)} — you may put one matching card
 *       onto the battlefield, rest on the bottom (Mayael the Anima; Mitotic Manipulation via
 *       {@code CardSharesNameWithAPermanentPredicate}).</li>
 *   <li>{@link #mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(int, CardPredicate)} — you
 *       may put any number of matching cards onto the battlefield; the rest go on the bottom in a
 *       random order (Nissa, Genesis Mage −10).</li>
 *   <li>{@link #putOneOnTopRestOnBottom(int)} — put one back on top, rest on the bottom (Cream of
 *       the Crop's materialised trigger).</li>
 *   <li>{@link #mayPutOneOnTopRestToGraveyard(int)} — you may put one back on top, rest into the
 *       graveyard (Gutless Plunderer).</li>
 * </ul>
 *
 * <p>Still separate records (genuinely different mechanics): the hand/top/bottom three-way split
 * ({@code LookAtTopCardsHandTopBottomEffect}), the two-bounded-picks Gift of the Gargantuan / Benefaction
 * of Rhonas shape ({@code LookAtTopCardsRevealTwoTypesToHandThenRestEffect}), the attachment-relative
 * Call to the Kindred shape, the X-based multi-select-to-battlefield shape, and the target-library
 * family ({@link LookAtTopCardsOfTargetLibraryEffect}).
 *
 * @param lookCount         how many cards to look at / reveal from the top of the library
 * @param chooseCount       the maximum number of cards to choose
 * @param choosePredicate   when non-null, only matching cards are eligible to be chosen
 * @param restDestination   where the not-chosen cards go ({@code BOTTOM_OF_LIBRARY},
 *                          {@code BOTTOM_OF_LIBRARY_RANDOM}, {@code TOP_OF_LIBRARY},
 *                          {@code GRAVEYARD} or {@code EXILE})
 * @param reveal            when true the whole look is public (the looked-at cards are logged) —
 *                          distinct from the may-reveal flows, which reveal only the chosen cards
 * @param chosenDestination where the chosen cards go ({@code HAND}, {@code BATTLEFIELD} or
 *                          {@code TOP_OF_LIBRARY})
 * @param optional          when true the choice is a "may" — the player can decline / pick fewer,
 *                          and no auto-move-to-hand shortcut applies
 * @param gainLifeEqualToChosenCardManaValue when true, gain life equal to the mana value of the
 *                                           chosen card
 * @param chooseManaValueAtMost when non-null, only cards with mana value at most this
 *                              resolution-time amount are eligible for selection
 * @param effectIfNoCardChosen optional effect inserted after this effect when the optional choice
 *                             finds or selects no card
 * @param recordChosenCount when true, records the number of selected cards as the stack entry's
 *                          event value for a following effect
 */
public record LookAtTopCardsEffect(
        DynamicAmount lookCount,
        DynamicAmount chooseCount,
        CardPredicate choosePredicate,
        LookDestination restDestination,
        boolean reveal,
        LibrarySearchDestination chosenDestination,
        boolean optional,
        boolean gainLifeEqualToChosenCardManaValue,
        DynamicAmount chooseManaValueAtMost,
        CardEffect effectIfNoCardChosen,
        boolean recordChosenCount,
        int loseLifePerSelectedCard,
        LibrarySelectionFollowUp battlefieldSelectionFollowUp
) implements CardEffect {

    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination, boolean optional,
            boolean gainLifeEqualToChosenCardManaValue, DynamicAmount chooseManaValueAtMost,
            CardEffect effectIfNoCardChosen, boolean recordChosenCount) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal, chosenDestination,
                optional, gainLifeEqualToChosenCardManaValue, chooseManaValueAtMost,
                effectIfNoCardChosen, recordChosenCount, 0, null);
    }

    /** Canonical form without an effect for the no-card branch. */
    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination, boolean optional,
            boolean gainLifeEqualToChosenCardManaValue, DynamicAmount chooseManaValueAtMost) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal, chosenDestination,
                optional, gainLifeEqualToChosenCardManaValue, chooseManaValueAtMost, null, false);
    }

    /** Canonical form without the selected-count flag. */
    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination, boolean optional,
            boolean gainLifeEqualToChosenCardManaValue, DynamicAmount chooseManaValueAtMost,
            CardEffect effectIfNoCardChosen) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal, chosenDestination,
                optional, gainLifeEqualToChosenCardManaValue, chooseManaValueAtMost,
                effectIfNoCardChosen, false);
    }

    /** Mandatory choose-to-hand shape (the original 5-field form). */
    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal,
                LibrarySearchDestination.HAND, false, false, null);
    }

    /** Canonical form without the optional chosen-card life-gain rider. */
    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination, boolean optional) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal,
                chosenDestination, optional, false, null);
    }

    /** Canonical form without a mana-value cap. */
    public LookAtTopCardsEffect(DynamicAmount lookCount, DynamicAmount chooseCount,
            CardPredicate choosePredicate, LookDestination restDestination, boolean reveal,
            LibrarySearchDestination chosenDestination, boolean optional,
            boolean gainLifeEqualToChosenCardManaValue) {
        this(lookCount, chooseCount, choosePredicate, restDestination, reveal,
                chosenDestination, optional, gainLifeEqualToChosenCardManaValue, null);
    }

    /** One card to hand, the rest on the bottom of the library. */
    public static LookAtTopCardsEffect chooseOneToHandRestOnBottom(DynamicAmount lookCount) {
        return new LookAtTopCardsEffect(lookCount, new Fixed(1), null,
                LookDestination.BOTTOM_OF_LIBRARY, false);
    }

    /**
     * Up to {@code chooseCount} cards to hand; the rest go on the bottom of the library in a
     * random order (no player reorder). Memory Deluge.
     */
    public static LookAtTopCardsEffect chooseNToHandRestOnBottomRandom(
            DynamicAmount lookCount, int chooseCount) {
        return new LookAtTopCardsEffect(lookCount, new Fixed(chooseCount), null,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false);
    }

    /** One card to hand, the rest back on top of the library in any order (Diabolic Vision). */
    public static LookAtTopCardsEffect chooseOneToHandRestOnTop(int lookCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), null,
                LookDestination.TOP_OF_LIBRARY, false);
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

    public static LookAtTopCardsEffect mayChooseAnyNumberToHandRestToGraveyardLoseLife(
            int lookCount, int lifeLossPerSelectedCard) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(lookCount), null,
                LookDestination.GRAVEYARD, false, LibrarySearchDestination.HAND, true,
                false, null, null, false, lifeLossPerSelectedCard, null);
    }

    /** Reveal the top cards, put one into hand, the rest into the graveyard, and gain life equal
     * to the chosen card's mana value (Reviving Vapors). */
    public static LookAtTopCardsEffect chooseOneToHandRestToGraveyardGainLifeEqualToManaValue(int lookCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), null,
                LookDestination.GRAVEYARD, true, LibrarySearchDestination.HAND, false, true);
    }

    /** Up to {@code chooseCount} matching cards to hand (optionally revealed), the rest into the graveyard. */
    public static LookAtTopCardsEffect chooseNToHandRestToGraveyard(
            int lookCount, int chooseCount, CardPredicate choosePredicate, boolean reveal) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(chooseCount), choosePredicate,
                LookDestination.GRAVEYARD, reveal);
    }

    /** You may reveal one matching card and put it into your hand; the rest go to the bottom. */
    public static LookAtTopCardsEffect mayRevealOneToHandRestOnBottom(int lookCount, CardPredicate choosePredicate) {
        return mayRevealUpToToHandRestOnBottom(lookCount, choosePredicate, 1);
    }

    /** You may reveal any number of matching cards and put them into your hand; the rest go to the bottom. */
    public static LookAtTopCardsEffect mayRevealAnyNumberToHandRestOnBottom(int lookCount, CardPredicate choosePredicate) {
        return mayRevealUpToToHandRestOnBottom(lookCount, choosePredicate, Integer.MAX_VALUE);
    }

    /** You may reveal up to {@code maxReveal} matching cards and put them into your hand; the rest go to the bottom. */
    public static LookAtTopCardsEffect mayRevealUpToToHandRestOnBottom(
            int lookCount, CardPredicate choosePredicate, int maxReveal) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(maxReveal), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY, false, LibrarySearchDestination.HAND, true);
    }

    /**
     * You may reveal one matching card and put it into your hand; the rest go on the bottom of the
     * library in a random order (no player reorder).
     */
    public static LookAtTopCardsEffect mayRevealOneToHandRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false, LibrarySearchDestination.HAND, true);
    }

    /** You may reveal one matching card into your hand; if none is taken, resolve a follow-up effect. */
    public static LookAtTopCardsEffect mayRevealOneToHandRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate, CardEffect effectIfNoCardChosen) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false, LibrarySearchDestination.HAND, true,
                false, null, effectIfNoCardChosen);
    }

    /**
     * Reveal the top {@code lookCount} cards publicly; you may put one matching card into your
     * hand; the rest go into your graveyard (Grisly Salvage).
     */
    public static LookAtTopCardsEffect mayRevealOneToHandRestToGraveyard(
            int lookCount, CardPredicate choosePredicate) {
        return mayRevealUpToToHandRestToGraveyard(lookCount, choosePredicate, new Fixed(1));
    }

    /**
     * Reveal the top {@code lookCount} cards publicly; you may put up to {@code chooseCount}
     * matching cards into your hand; the rest go into your graveyard. The dynamic choose count
     * carries riders that only change the number kept (Gather the Pack's spell mastery).
     */
    public static LookAtTopCardsEffect mayRevealUpToToHandRestToGraveyard(
            int lookCount, CardPredicate choosePredicate, DynamicAmount chooseCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), chooseCount, choosePredicate,
                LookDestination.GRAVEYARD, true, LibrarySearchDestination.HAND, true);
    }

    /** You may put one matching card onto the battlefield; the rest go to the bottom. */
    public static LookAtTopCardsEffect mayPutMatchingOntoBattlefield(int lookCount, CardPredicate choosePredicate) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY, false, LibrarySearchDestination.BATTLEFIELD, true);
    }

    /** Put one matching card onto the battlefield and the rest into the graveyard. */
    public static LookAtTopCardsEffect putOneMatchingOntoBattlefieldRestToGraveyard(
            int lookCount, CardPredicate choosePredicate) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.GRAVEYARD, true, LibrarySearchDestination.BATTLEFIELD, false);
    }

    /** You may put one matching card onto the battlefield; the rest go to the bottom randomly. */
    public static LookAtTopCardsEffect mayPutMatchingOntoBattlefieldRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false, LibrarySearchDestination.BATTLEFIELD, true);
    }

    /** You may put one matching card within a dynamic mana-value cap onto the battlefield. */
    public static LookAtTopCardsEffect mayPutMatchingOntoBattlefieldRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate, DynamicAmount chooseManaValueAtMost) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD, true, false, chooseManaValueAtMost);
    }

    /** You may put one matching card onto the battlefield tapped; the rest go to the bottom randomly. */
    public static LookAtTopCardsEffect mayPutOneMatchingOntoBattlefieldRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD_TAPPED, true);
    }

    /**
     * You may put any number of matching cards onto the battlefield; the rest go on the bottom of
     * the library in a random order (Nissa, Genesis Mage −10).
     */
    public static LookAtTopCardsEffect mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate) {
        return mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(lookCount, choosePredicate, null);
    }

    /** You may put any number of matching cards onto the battlefield and create a reflexive follow-up. */
    public static LookAtTopCardsEffect mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate, LibrarySelectionFollowUp followUp) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(lookCount), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD, true, false, null, null, false, 0, followUp);
    }

    /** You may put up to {@code maxCount} matching cards onto the battlefield tapped; the rest go
     * to the bottom randomly, optionally recording the selected count for a following effect. */
    public static LookAtTopCardsEffect mayPutUpToMatchingOntoBattlefieldTappedRestOnBottomRandom(
            int lookCount, CardPredicate choosePredicate, int maxCount, boolean recordChosenCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(maxCount), choosePredicate,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD_TAPPED, true, false, null, null,
                recordChosenCount);
    }

    /** Put one of the looked-at cards on top of your library and the rest on the bottom (Cream of the Crop). */
    public static LookAtTopCardsEffect putOneOnTopRestOnBottom(int lookCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), null,
                LookDestination.BOTTOM_OF_LIBRARY, false, LibrarySearchDestination.TOP_OF_LIBRARY, false);
    }

    /** You may put one of the looked-at cards on top of your library and the rest into your graveyard. */
    public static LookAtTopCardsEffect mayPutOneOnTopRestToGraveyard(int lookCount) {
        return new LookAtTopCardsEffect(new Fixed(lookCount), new Fixed(1), null,
                LookDestination.GRAVEYARD, false, LibrarySearchDestination.TOP_OF_LIBRARY, true);
    }
}
