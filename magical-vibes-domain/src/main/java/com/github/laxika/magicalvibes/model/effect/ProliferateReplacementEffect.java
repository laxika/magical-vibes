package com.github.laxika.magicalvibes.model.effect;

/** Replacement behavior for proliferate events performed by the effect's controller. */
public interface ProliferateReplacementEffect extends CardEffect {

    int replace(int count);
}
