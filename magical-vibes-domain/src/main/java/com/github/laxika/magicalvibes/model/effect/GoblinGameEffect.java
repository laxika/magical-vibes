package com.github.laxika.magicalvibes.model.effect;

/** Each player secretly chooses a number, then the revealed numbers determine the result. */
public record GoblinGameEffect(boolean highestNumberWins, int countersOnSourceIfControllerWins,
                               boolean discardNonLowestAndDrawSeven)
        implements CardEffect {

    public GoblinGameEffect() {
        this(false, 0, false);
    }

    public GoblinGameEffect(boolean highestNumberWins, int countersOnSourceIfControllerWins) {
        this(highestNumberWins, countersOnSourceIfControllerWins, false);
    }

    public static GoblinGameEffect wheelOfMisfortune() {
        return new GoblinGameEffect(false, 0, true);
    }
}
