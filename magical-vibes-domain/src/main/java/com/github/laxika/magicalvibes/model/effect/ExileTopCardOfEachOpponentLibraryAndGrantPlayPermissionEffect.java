package com.github.laxika.magicalvibes.model.effect;

/** Exiles each qualifying opponent's top card and lets the controller play it while it remains exiled. */
public record ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect(int minimumPoisonCounters)
        implements CardEffect {

    /** Creates the unrestricted Brainstealer Dragon version of the effect. */
    public ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect() {
        this(0);
    }
}
