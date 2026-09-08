package com.github.laxika.magicalvibes.model.effect;

/** Tekuthal's static replacement effect: proliferate twice instead of once. */
public record DoubleProliferateEffect() implements ProliferateReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count * 2 : count;
    }
}
