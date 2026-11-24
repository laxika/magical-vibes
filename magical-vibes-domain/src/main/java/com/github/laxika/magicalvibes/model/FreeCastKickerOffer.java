package com.github.laxika.magicalvibes.model;

/**
 * The state of an exile free cast that paused to offer the spell's kicker cost, or to ask for the mana of its cost
 * increase, so the cast resumes with the same options once the controller answers.
 */
public record FreeCastKickerOffer(boolean grantHaste, boolean returnToHandIfUnable, boolean suspendHaste,
                                  boolean completeInput, int face) {
}
