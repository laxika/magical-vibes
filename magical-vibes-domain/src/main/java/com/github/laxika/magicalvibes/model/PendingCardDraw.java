package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A draw waiting in {@code GameData.pendingCardDraws} behind an open draw choice: one draw of an instruction already
 * under way ({@link #single}), or a whole draw instruction that hasn't started ({@link #instruction}), whose
 * replacement effects (Quantum Riddler's extra card, an Alms Collector of the drawer's choice) apply only once it is
 * performed (CR 614.6, 616.1).
 *
 * @param playerId                       who draws
 * @param instructionAmount              how many cards the waiting instruction draws; {@code 0} for a single draw
 * @param skipsOpponentDrawTwoOrMoreReplacement whether the instruction is one an Alms Collector already made, so that
 *                                       replacement doesn't apply to it again (CR 614.5)
 */
public record PendingCardDraw(UUID playerId, int instructionAmount, boolean skipsOpponentDrawTwoOrMoreReplacement) {

    /** One draw of an instruction already under way. */
    public static PendingCardDraw single(UUID playerId) {
        return new PendingCardDraw(playerId, 0, false);
    }

    /** A whole draw instruction of {@code amount} cards that hasn't started. */
    public static PendingCardDraw instruction(UUID playerId, int amount, boolean skipsOpponentDrawTwoOrMoreReplacement) {
        return new PendingCardDraw(playerId, amount, skipsOpponentDrawTwoOrMoreReplacement);
    }

    /** Whether this is a whole instruction rather than one draw of an instruction already under way. */
    public boolean isInstruction() {
        return instructionAmount > 0;
    }
}
