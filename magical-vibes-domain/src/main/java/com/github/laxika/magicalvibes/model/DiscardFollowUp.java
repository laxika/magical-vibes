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
 * {@code eachPlayerAmounts} (when non-empty) overrides the shared {@code eachPlayerAmount} with a
 * per-chooser amount parallel to {@code remainingEachPlayerDiscards}, so different players can
 * discard different counts ("each player discards a third of their hand", Pox).
 */
public record DiscardFollowUp(int rummageDrawCount, UUID untapPermanentId,
                              List<UUID> remainingEachPlayerDiscards,
                              UUID eachPlayerControllerId, int eachPlayerAmount,
                              int graveyardReturnCount, List<Integer> eachPlayerAmounts) {

    public static final DiscardFollowUp NONE = new DiscardFollowUp(0, null, List.of(), null, 0, 0, List.of());

    public DiscardFollowUp {
        remainingEachPlayerDiscards = List.copyOf(remainingEachPlayerDiscards);
        eachPlayerAmounts = List.copyOf(eachPlayerAmounts);
    }

    public static DiscardFollowUp rummage(int drawCount) {
        return new DiscardFollowUp(drawCount, null, List.of(), null, 0, 0, List.of());
    }

    public static DiscardFollowUp untap(UUID permanentId) {
        return new DiscardFollowUp(0, permanentId, List.of(), null, 0, 0, List.of());
    }

    public static DiscardFollowUp eachPlayer(List<UUID> remainingChoosers, UUID controllerId, int amount) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, amount, 0, List.of());
    }

    /**
     * Each-player discard where each chooser discards their own amount ({@code amounts} parallel to
     * {@code remainingChoosers}). Used when the count is computed per player (Pox: a third of each
     * player's own hand, rounded up).
     */
    public static DiscardFollowUp eachPlayerVariableAmounts(List<UUID> remainingChoosers, UUID controllerId,
            List<Integer> amounts) {
        return new DiscardFollowUp(0, null, remainingChoosers, controllerId, 0, 0, amounts);
    }

    /** Return that many cards from the controller's graveyard to hand once the discard completes. */
    public static DiscardFollowUp graveyardReturn(int returnCount) {
        return new DiscardFollowUp(0, null, List.of(), null, 0, returnCount, List.of());
    }

    /**
     * The same follow-up with the each-player remainder (both choosers and their per-player amounts)
     * advanced past the current chooser.
     */
    public DiscardFollowUp withRemainingEachPlayer(List<UUID> remaining, List<Integer> remainingAmounts) {
        return new DiscardFollowUp(rummageDrawCount, untapPermanentId, remaining,
                eachPlayerControllerId, eachPlayerAmount, graveyardReturnCount, remainingAmounts);
    }
}
