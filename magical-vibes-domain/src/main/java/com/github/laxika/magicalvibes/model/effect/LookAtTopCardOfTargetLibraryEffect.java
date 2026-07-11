package com.github.laxika.magicalvibes.model.effect;

/**
 * "Look at the top card of target player's library." Private, informational look — the top card is
 * shown only to the controller and stays on top of the library (nothing moves, nothing is revealed
 * to opponents). Targets a player via {@code target(PlayerPredicateTargetFilter)}. Used by Dewdrop Spy.
 */
public record LookAtTopCardOfTargetLibraryEffect() implements CardEffect {
}
