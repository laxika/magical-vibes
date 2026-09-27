package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the
 * associated card from their hand without paying its mana cost.
 * Used by Counterlash (one PendingMayAbility per eligible hand card).
 *
 * @param revealCardOnDecline whether declining the cast publicly identifies the card
 * @param exileInsteadOfGraveyard whether the spell is exiled instead of going to a graveyard
 *                                after this free cast
 * @param afterSuccessfulCastEffect optional follow-up queued after a successful cast
 */
public record MayCastFromHandWithoutPayingManaCostEffect(
        boolean revealCardOnDecline,
        UUID choiceGroupId,
        CardEffect declineEffect,
        boolean exileInsteadOfGraveyard,
        CardEffect afterSuccessfulCastEffect
) implements CardEffect {

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
}
