package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the
 * associated card from their hand without paying its mana cost.
 * Used by Counterlash (one PendingMayAbility per eligible hand card).
 *
 * @param revealCardOnDecline whether declining the cast publicly identifies the card
 * @param exileInsteadOfGraveyard whether the spell is exiled instead of going to a graveyard
 *                                after this free cast
 * @param afterSuccessfulCastEffect optional "if you do" follow-up, resolved as part of this ability once the spell is cast
 * @param castableSplitModes for a split card, the modes of its modal spell (its halves, or its
 *                           fused spell) the permission lets it be cast as, or null for any. A
 *                           permission for a spell with certain characteristics considers only the
 *                           half being cast (CR 709.3a).
 * @param nextOffers for "up to N" / "any number of spells": the next round of offers, put into the resolving ability
 *                   once this card's cast begins, so the caster casts the spells in the order of their choice
 *                   (CR 608.2g); {@code null} for a single spell
 */
public record MayCastFromHandWithoutPayingManaCostEffect(
        boolean revealCardOnDecline,
        UUID choiceGroupId,
        CardEffect declineEffect,
        boolean exileInsteadOfGraveyard,
        CardEffect afterSuccessfulCastEffect,
        List<Integer> castableSplitModes,
        boolean castAsAdventure,
        CardEffect nextOffers
) implements CardEffect {

    public MayCastFromHandWithoutPayingManaCostEffect {
        castableSplitModes = castableSplitModes == null ? null : List.copyOf(castableSplitModes);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline,
                                                       UUID choiceGroupId,
                                                       CardEffect declineEffect,
                                                       boolean exileInsteadOfGraveyard,
                                                       CardEffect afterSuccessfulCastEffect) {
        this(revealCardOnDecline, choiceGroupId, declineEffect, exileInsteadOfGraveyard,
                afterSuccessfulCastEffect, null, false, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect() {
        this(true, null, null, false, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline) {
        this(revealCardOnDecline, null, null, false, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline,
                                                       boolean exileInsteadOfGraveyard) {
        this(revealCardOnDecline, null, null, exileInsteadOfGraveyard, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline,
                                                       UUID choiceGroupId,
                                                       CardEffect declineEffect) {
        this(revealCardOnDecline, choiceGroupId, declineEffect, false, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline,
                                                       CardEffect afterSuccessfulCastEffect) {
        this(revealCardOnDecline, null, null, false, afterSuccessfulCastEffect);
    }

    /** This permission letting a split card be cast only as {@code modes}. */
    public MayCastFromHandWithoutPayingManaCostEffect withCastableSplitModes(List<Integer> modes) {
        return new MayCastFromHandWithoutPayingManaCostEffect(revealCardOnDecline, choiceGroupId, declineEffect,
                exileInsteadOfGraveyard, afterSuccessfulCastEffect, modes, castAsAdventure, nextOffers);
    }

    /** This permission casting the adventurer card as its Adventure (CR 716.3). */
    public MayCastFromHandWithoutPayingManaCostEffect asAdventure() {
        return new MayCastFromHandWithoutPayingManaCostEffect(revealCardOnDecline, choiceGroupId, declineEffect,
                exileInsteadOfGraveyard, afterSuccessfulCastEffect, castableSplitModes, true, nextOffers);
    }

    /** This offer followed, once its cast begins, by {@code offers} (the next round of an "up to N" choice). */
    public MayCastFromHandWithoutPayingManaCostEffect thenOffering(CardEffect offers) {
        return new MayCastFromHandWithoutPayingManaCostEffect(revealCardOnDecline, choiceGroupId, declineEffect,
                exileInsteadOfGraveyard, afterSuccessfulCastEffect, castableSplitModes, castAsAdventure, offers);
    }
}
