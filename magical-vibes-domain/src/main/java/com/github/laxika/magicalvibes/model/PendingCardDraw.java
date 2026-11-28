package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A draw waiting in {@code GameData.pendingCardDraws} behind an open draw choice: one draw of an instruction already
 * under way ({@link #single}), or a whole draw instruction that hasn't started ({@link #instruction}), whose
 * replacement effects (Quantum Riddler's extra card, an Alms Collector of the drawer's choice) apply only once it is
 * performed (CR 614.6, 616.1); or the end of a modified event whose draws waited ({@link #endOfModifiedEvent}).
 *
 * @param playerId                       who draws; {@code null} for the end of a modified event
 * @param instructionAmount              how many cards the waiting instruction draws; {@code 0} for a single draw
 * @param skipsOpponentDrawTwoOrMoreReplacement whether the instruction is one an Alms Collector already made, so that
 *                                       replacement doesn't apply to it again (CR 614.5)
 * @param endsModifiedEvent              whether this marks where the draws of a modified event that waited behind a
 *                                       draw choice end, so the draws before it are drawn inside that event
 */
public record PendingCardDraw(UUID playerId, int instructionAmount, boolean skipsOpponentDrawTwoOrMoreReplacement,
                              boolean endsModifiedEvent) {

    /** One draw of an instruction already under way. */
    public static PendingCardDraw single(UUID playerId) {
        return new PendingCardDraw(playerId, 0, false, false);
    }

    /** A whole draw instruction of {@code amount} cards that hasn't started. */
    public static PendingCardDraw instruction(UUID playerId, int amount, boolean skipsOpponentDrawTwoOrMoreReplacement) {
        return new PendingCardDraw(playerId, amount, skipsOpponentDrawTwoOrMoreReplacement, false);
    }

    /**
     * The end of a modified event (an Alms Collector's two draws) whose draws waited behind a draw choice: reached
     * after them, it leaves the event, so a later draw is again the draw its instruction asked for (CR 614.6).
     */
    public static PendingCardDraw endOfModifiedEvent() {
        return new PendingCardDraw(null, 0, false, true);
    }

    /** Whether this is a whole instruction rather than one draw of an instruction already under way. */
    public boolean isInstruction() {
        return instructionAmount > 0;
    }
}
