package com.github.laxika.magicalvibes.model.effect;

/**
 * "Look at the top {@code count} cards of target player's library." Private, informational look — the
 * cards are shown only to the controller and stay on top of the library in their original order
 * (nothing moves, nothing is revealed to opponents). Targets a player via
 * {@code target(PlayerPredicateTargetFilter)} or an {@code ActivatedAbility} player target filter.
 * Used by Dewdrop Spy (count 1) and Orcish Spy (count 3).
 */
public record LookAtTopCardOfTargetLibraryEffect(int count) implements CardEffect {

    /** Convenience for the common single-card look (Dewdrop Spy). */
    public LookAtTopCardOfTargetLibraryEffect() {
        this(1);
    }
}
