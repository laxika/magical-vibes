package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over work attached to a {@link PendingInteraction.DiscardChoice} and performed (or
 * continued) when the discard sequence completes. Rides the fresh-record-per-pick re-begins,
 * replacing the per-mechanic {@code pending*} fields {@code GameData} used to hold:
 * {@code rummageDrawCount} draws that many cards afterwards ("discard, then draw");
 * {@code untapPermanentId} untaps the source afterwards ("discard a card, then untap [source]");
 * {@code graveyardReturnCount} returns that many cards from the controller's graveyard to hand
 * afterwards ("discard X cards, then return a card for each discarded", Recall);
 * the each-player trio is the APNAP remainder of an "each player discards" flow, advanced by
 * {@code PlayerInteractionSupport.startNextEachPlayerDiscard} after each player finishes.
 */
public record DiscardFollowUp(int rummageDrawCount, UUID untapPermanentId,
                              List<UUID> remainingEachPlayerDiscards,
                              UUID eachPlayerControllerId, int eachPlayerAmount,
                              int graveyardReturnCount) {

    public static final DiscardFollowUp NONE = new DiscardFollowUp(0, null, List.of(), null, 0, 0);

    public DiscardFollowUp {
        remainingEachPlayerDiscards = List.copyOf(remainingEachPlayerDiscards);
    }

    public static DiscardFollowUp rummage(int drawCount) {
        return new DiscardFollowUp(drawCount, null, List.of(), null, 0, 0);
    }

    public static DiscardFollowUp untap(UUID permanentId) {
        return new DiscardFollowUp(0, permanentId, List.of(), null, 0, 0);
    }

    public static DiscardFollowUp eachPlayer(List<UUID> remainingChoosers, UUID controllerId, int amount) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, amount, 0);
    }

    /** Return that many cards from the controller's graveyard to hand once the discard completes. */
    public static DiscardFollowUp graveyardReturn(int returnCount) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, returnCount);
    }

    /** The same follow-up with the each-player remainder advanced past the current chooser. */
    public DiscardFollowUp withRemainingEachPlayerDiscards(List<UUID> remaining) {
        return new DiscardFollowUp(rummageDrawCount, untapPermanentId, remaining,
                eachPlayerControllerId, eachPlayerAmount, graveyardReturnCount);
    }
}
