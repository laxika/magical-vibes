package com.github.laxika.magicalvibes.model.effect;

/** Each player secretly chooses a positive number, then the revealed numbers determine life loss. */
public record GoblinGameEffect(boolean highestNumberWins, int countersOnSourceIfControllerWins)
        implements CardEffect {

    public GoblinGameEffect() {
        this(false, 0);
    }
}
