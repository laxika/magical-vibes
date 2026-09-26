package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top cards of the controller's library, then offers two independent optional picks:
 * one permanent to the battlefield with an as-entered replacement and one permanent to hand.
 * All cards not chosen by either pick are put into the graveyard.
 */
public record LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect(
        int count,
        EnterWithCountersEffect battlefieldEntryReplacement
) implements CardEffect {

    public LookAtTopCardsMayPutPermanentToBattlefieldAndHandRestToGraveyardEffect(
            int count, EnterWithCountersEffect battlefieldEntryReplacement) {
        this.count = Math.max(0, count);
        this.battlefieldEntryReplacement = battlefieldEntryReplacement;
    }
}
