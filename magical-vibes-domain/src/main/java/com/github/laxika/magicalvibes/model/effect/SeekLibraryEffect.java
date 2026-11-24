package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Seek [N] [filter] cards": up to {@code count} matching cards, each chosen at random from among the matching cards
 * of the seeker's library, are put into the seeker's hand (digital only, MTG Arena's definition; it isn't in the CR).
 * Seek gives no player a choice and doesn't shuffle the library. With another {@code destination} the sought cards go
 * on from the hand: "seek ... and put it onto the battlefield" (so a "can't enter from libraries" rule doesn't stop it),
 * "... and put them into your graveyard", "... and exile it". A card a filter's {@code CardSharesCardTypeWithCardsPredicate}
 * or {@code CardManaValueAtMostAmountPredicate} reads ("a card that shares a card type with the exiled card", "a card
 * with greater mana value than the discarded card") is bound as the seek applies.
 *
 * <p>Records {@code StepKind.SEEK}: the sought cards, each in the zone it went to (a hand, a graveyard, exile, or the
 * zone a replacement put it in instead), or the permanent it became on the battlefield, and the seekers: "it
 * perpetually gets +1/+1", "manifest it", "choose one of them", "if a card ... is put onto the battlefield this way".
 *
 * <p>When {@code triggersSeekAbilities} is false the cards are taken at random without being sought ("put a random
 * land card from your library into your graveyard"), so "whenever you seek" abilities do not trigger and nothing
 * passes through the hand (Tezzeret's Reckoning exiles random cards, and
 * {@link #randomExileWithSource(DynamicAmount, CardPredicate, boolean)} builds that form); the step is recorded all the
 * same.
 *
 * @param fromAmongTop "from among the top N cards of your library"; {@code null} for the whole library
 * @param seeker       who seeks ("the exiled card's owner seeks"); {@code null} for the controller
 */
public record SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                                LibrarySearchDestination destination, ManaValueBound manaValueBound,
                                boolean faceDown, boolean grantPlayUntilNextTurn, CardSubtype grantSubtype,
                                boolean battlefieldCloaked, boolean triggersSeekAbilities,
                                DynamicAmount fromAmongTop, PlayerSubject seeker)
        implements CardEffect {

    public SeekLibraryEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
        if (seeker != null && seeker.declaresTarget()) {
            throw new IllegalArgumentException("Who seeks is no target of the seek: " + seeker);
        }
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, boolean grantPlayUntilNextTurn, CardSubtype grantSubtype,
                             boolean battlefieldCloaked, boolean triggersSeekAbilities) {
        this(count, filter, destination, manaValueBound, faceDown, grantPlayUntilNextTurn, grantSubtype,
                battlefieldCloaked, triggersSeekAbilities, null, null);
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(nonNegative(count)), filter, destination, null, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination) {
        this(count, filter, destination, null, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound) {
        this(count, filter, destination, manaValueBound, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, false, false, grantSubtype, false, true);
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination,
                             ManaValueBound manaValueBound, CardSubtype grantSubtype) {
        this(new Fixed(nonNegative(count)), filter, destination, manaValueBound, false, false, grantSubtype,
                false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown) {
        this(count, filter, destination, manaValueBound, faceDown, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, boolean grantPlayUntilNextTurn) {
        this(count, filter, destination, manaValueBound, faceDown, grantPlayUntilNextTurn, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, faceDown, false, grantSubtype, false, true);
    }

    public SeekLibraryEffect(int count, CardPredicate filter) {
        this(new Fixed(nonNegative(count)), filter, LibrarySearchDestination.HAND, null, false, false, null,
                false, true);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue, boolean entersTapped) {
        this(new Fixed(1), filter,
                entersTapped ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                        : LibrarySearchDestination.BATTLEFIELD,
                new ManaValueBound(new Fixed(maxManaValue), false, 0), false, false, null, false, true);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue) {
        this(filter, maxManaValue, false);
    }

    public static SeekLibraryEffect cloakedToBattlefield(DynamicAmount count, CardPredicate filter,
                                                          ManaValueBound manaValueBound) {
        return new SeekLibraryEffect(count, filter, LibrarySearchDestination.BATTLEFIELD,
                manaValueBound, false, false, null, true, true);
    }

    /**
     * Randomly takes {@code count} matching cards from the library and exiles them tracked with the
     * source, without seeking them: "whenever you seek" abilities do not trigger.
     */
    public static SeekLibraryEffect randomExileWithSource(DynamicAmount count, CardPredicate filter,
                                                          boolean faceDown) {
        return new SeekLibraryEffect(count, filter, LibrarySearchDestination.EXILE_WITH_SOURCE, null,
                faceDown, false, null, false, false);
    }

    /**
     * Randomly takes {@code count} matching cards from the library and puts them into the destination
     * without seeking them: "whenever you seek" abilities do not trigger.
     */
    public static SeekLibraryEffect randomWithoutSeeking(DynamicAmount count, CardPredicate filter,
                                                         LibrarySearchDestination destination,
                                                         ManaValueBound manaValueBound) {
        return new SeekLibraryEffect(count, filter, destination, manaValueBound, false, false, null, false,
                false);
    }

    /**
     * "Seek [it] and exile it. Until the end of your next turn, you may play the exiled card": the sought card goes
     * from the hand into exile, playable by the seeker until the end of their next turn.
     */
    public static SeekLibraryEffect exiledPlayableUntilNextTurn(int count, CardPredicate filter) {
        return new SeekLibraryEffect(new Fixed(nonNegative(count)), filter, LibrarySearchDestination.EXILE, null,
                false, true, null, false, true);
    }

    /** This seek "from among the top {@code count} cards of your library". */
    public SeekLibraryEffect fromAmongTop(int count) {
        return new SeekLibraryEffect(this.count, filter, destination, manaValueBound, faceDown,
                grantPlayUntilNextTurn, grantSubtype, battlefieldCloaked, triggersSeekAbilities,
                new Fixed(nonNegative(count)), seeker);
    }

    /** This seek made by the player {@code seeker} names, in their library ("the exiled card's owner seeks"). */
    public SeekLibraryEffect by(PlayerSubject seeker) {
        return new SeekLibraryEffect(count, filter, destination, manaValueBound, faceDown, grantPlayUntilNextTurn,
                grantSubtype, battlefieldCloaked, triggersSeekAbilities, fromAmongTop,
                Objects.requireNonNull(seeker, "seeker"));
    }

    private static int nonNegative(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        return count;
    }
}
