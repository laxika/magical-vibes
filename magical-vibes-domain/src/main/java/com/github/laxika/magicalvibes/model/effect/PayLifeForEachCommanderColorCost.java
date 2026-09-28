package com.github.laxika.magicalvibes.model.effect;

/** Ability-only cost for paying one life for each color in the activating player's commander color identity. */
public record PayLifeForEachCommanderColorCost() implements CostEffect {

    @Override
    public boolean paysLifeForEachCommanderColorIdentity() {
        return true;
    }
}
