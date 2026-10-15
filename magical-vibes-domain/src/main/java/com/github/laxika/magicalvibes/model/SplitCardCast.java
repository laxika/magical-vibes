package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * A split card being cast during resolution or through a "you may cast" permission. The half is
 * chosen as it is cast, before the spell is put onto the stack (CR 709.3), and only that half is
 * evaluated to see whether it can be cast (CR 709.3a).
 *
 * @param physicalCard              the split card, already removed from {@code sourceZone}; a card
 *                                  cast from exile stays there until the spell is cast
 * @param sourceIndex               the card's position in its zone, so it can return there when
 *                                  the spell can't be cast
 * @param ownerId                   the owner of the zone the card is cast from
 * @param maxManaValue              the largest mana value the cast half may have
 * @param afterSuccessfulCastEffect effect queued once the spell is cast, or null
 * @param sourcePermanentId         the permanent whose ability lets the card be cast, or null
 * @param payManaCost               whether the caster pays the chosen half's mana cost
 * @param anyManaType               whether that cost may be paid with mana of any type
 * @param copyCount                 copies of the spell created once it is cast
 * @param restrictAdditionalSpellsThisTurn whether the caster can't cast other spells this turn once
 *                                         this one is cast
 * @param allowedModes              the modes of the card's modal spell (its halves, or its fused
 *                                  spell) it may be cast as, or null for any
 * @param origin                    how a card cast from exile, from outside the game, held outside
 *                                  every zone, or played by Word of Command is cast; null for a card
 *                                  set aside from its hand, graveyard or library
 * @param alternativeManaCost       the mana cost paid instead of the chosen half's, such as a miracle
 *                                  cost, or null to pay the half's own
 */
public record SplitCardCast(UUID controllerId, Card physicalCard, Zone sourceZone, int sourceIndex,
                            UUID ownerId, boolean exileInsteadOfGraveyard, int maxManaValue,
                            CardEffect afterSuccessfulCastEffect, UUID sourcePermanentId,
                            boolean payManaCost, boolean anyManaType, int copyCount,
                            boolean restrictAdditionalSpellsThisTurn, List<Integer> allowedModes,
                            Origin origin, String alternativeManaCost) {

    public SplitCardCast {
        allowedModes = allowedModes == null ? null : List.copyOf(allowedModes);
    }

    /** A cast without paying the card's mana cost. */
    public SplitCardCast(UUID controllerId, Card physicalCard, Zone sourceZone, int sourceIndex,
                         UUID ownerId, boolean exileInsteadOfGraveyard, int maxManaValue,
                         CardEffect afterSuccessfulCastEffect, UUID sourcePermanentId) {
        this(controllerId, physicalCard, sourceZone, sourceIndex, ownerId, exileInsteadOfGraveyard,
                maxManaValue, afterSuccessfulCastEffect, sourcePermanentId, false, false, 0, false, null, null, null);
    }

    /** This cast paying the chosen half's mana cost, with mana of any type when {@code anyManaType}. */
    public SplitCardCast payingManaCost(boolean anyManaType) {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                true, anyManaType, copyCount, restrictAdditionalSpellsThisTurn, allowedModes,
                origin, alternativeManaCost);
    }

    /** This cast followed by {@code copyCount} copies of the spell. */
    public SplitCardCast withCopies(int copyCount) {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                payManaCost, anyManaType, copyCount, restrictAdditionalSpellsThisTurn, allowedModes,
                origin, alternativeManaCost);
    }

    /** This cast stopping its caster from casting other spells this turn. */
    public SplitCardCast restrictingAdditionalSpellsThisTurn() {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                payManaCost, anyManaType, copyCount, true, allowedModes, origin, alternativeManaCost);
    }

    /** This cast limited to the given modes of the card's modal spell, or to any when null. */
    public SplitCardCast limitedToModes(List<Integer> modes) {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                payManaCost, anyManaType, copyCount, restrictAdditionalSpellsThisTurn, modes,
                origin, alternativeManaCost);
    }

    /** This cast paying {@code manaCost} instead of the chosen half's mana cost. */
    public SplitCardCast payingAlternativeCost(String manaCost) {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                true, anyManaType, copyCount, restrictAdditionalSpellsThisTurn, allowedModes, origin, manaCost);
    }

    /** This cast from {@code origin}. */
    public SplitCardCast from(Origin origin) {
        return new SplitCardCast(controllerId, physicalCard, sourceZone, sourceIndex, ownerId,
                exileInsteadOfGraveyard, maxManaValue, afterSuccessfulCastEffect, sourcePermanentId,
                payManaCost, anyManaType, copyCount, restrictAdditionalSpellsThisTurn, allowedModes,
                origin, alternativeManaCost);
    }

    /** How the card is cast from exile, or null when it isn't. */
    public ExileCast exileCast() {
        return origin instanceof ExileCast exileCast ? exileCast : null;
    }

    /** How the revealed card is cast, or null when it isn't a revealed card held outside every zone. */
    public RevealedCast revealedCast() {
        return origin instanceof RevealedCast revealedCast ? revealedCast : null;
    }

    /** Where a card that isn't simply set aside from its zone is cast from. */
    public sealed interface Origin permits ExileCast, RevealedCast, WordOfCommandCast, OutsideGameCast {
    }

    /**
     * How a split card is cast from exile.
     *
     * @param copy                         whether the card is a copy, which ceases to exist if it
     *                                     isn't cast (CR 707.10a)
     * @param putOnLibraryBottomInsteadOfGraveyard whether the spell goes to the bottom of its owner's
     *                                     library instead of a graveyard
     * @param payLifeEqualToManaValue      whether life equal to the chosen half's mana value is paid
     *                                     instead of its mana cost
     * @param lifeLossOnCast               life the caster loses once the spell is cast
     * @param putIntoHandIfNotCast         whether a card that isn't cast is put into its owner's hand
     *                                     instead of staying exiled
     */
    public record ExileCast(boolean copy, boolean putOnLibraryBottomInsteadOfGraveyard,
                            boolean payLifeEqualToManaValue, int lifeLossOnCast,
                            boolean putIntoHandIfNotCast) implements Origin {

        /** An exiled card, which stays exiled if it isn't cast. */
        public static ExileCast ofCard() {
            return new ExileCast(false, false, false, 0, false);
        }

        /** An exiled copy, which ceases to exist if it isn't cast. */
        public static ExileCast ofCopy() {
            return new ExileCast(true, false, false, 0, false);
        }

        /** This cast, putting the spell on the bottom of its owner's library instead of a graveyard. */
        public ExileCast puttingOnLibraryBottom(boolean libraryBottom) {
            return new ExileCast(copy, libraryBottom, payLifeEqualToManaValue, lifeLossOnCast, putIntoHandIfNotCast);
        }

        /** This cast, paying life equal to the half's mana value instead of its mana cost when {@code life}. */
        public ExileCast payingLifeEqualToManaValue(boolean life) {
            return new ExileCast(copy, putOnLibraryBottomInsteadOfGraveyard, life, lifeLossOnCast,
                    putIntoHandIfNotCast);
        }

        /** This cast, making the caster lose {@code amount} life once the spell is cast. */
        public ExileCast losingLifeOnCast(int amount) {
            return new ExileCast(copy, putOnLibraryBottomInsteadOfGraveyard, payLifeEqualToManaValue, amount,
                    putIntoHandIfNotCast);
        }

        /** This cast, putting a card that isn't cast into its owner's hand. */
        public ExileCast puttingIntoHandIfNotCast() {
            return new ExileCast(copy, putOnLibraryBottomInsteadOfGraveyard, payLifeEqualToManaValue,
                    lifeLossOnCast, true);
        }
    }

    /**
     * A card Word of Command has its owner play: it stays in their hand while its half and targets
     * are chosen, then is cast through the normal cast from hand, which pays its cost.
     */
    public record WordOfCommandCast() implements Origin {
    }

    /**
     * A card set aside from outside the game while its half and targets are chosen.
     *
     * @param paySideboardCastTax whether a free cast still pays the cast's generic cost increases and
     *                            reveals, as a free cast from a sideboard does
     */
    public record OutsideGameCast(boolean paySideboardCastTax) implements Origin {
    }

    /**
     * How a revealed card that has left its library, and is held outside every zone while it may be
     * cast, is cast (ripple, Talent of the Telepath, scrycast).
     *
     * @param cardsToBottom the other held cards, put on the bottom of the owner's library once this
     *                      cast is done; null when an uncast card goes to its owner's graveyard instead
     */
    public record RevealedCast(List<Card> cardsToBottom) implements Origin {

        public RevealedCast {
            cardsToBottom = cardsToBottom == null ? null : List.copyOf(cardsToBottom);
        }
    }

    /**
     * The chosen half, prepared for the stack, while its targets are chosen one at a time.
     *
     * @param cardToCast    the runtime copy with only the chosen half's characteristics
     * @param spellEffects  the chosen half's effects
     * @param modeEncoding  the chosen half as an encoded mode selection of the card's modal spell
     * @param chosenTargets the targets chosen so far, in declaration order
     */
    public record Targeting(SplitCardCast cast, Card cardToCast, StackEntryType spellType,
                            List<CardEffect> spellEffects, int xValue, int modeEncoding,
                            List<UUID> chosenTargets) {

        public Targeting {
            spellEffects = List.copyOf(spellEffects);
            chosenTargets = List.copyOf(chosenTargets);
        }
    }
}
