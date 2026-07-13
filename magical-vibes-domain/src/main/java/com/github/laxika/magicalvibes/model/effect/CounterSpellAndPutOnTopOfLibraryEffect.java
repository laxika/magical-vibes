package com.github.laxika.magicalvibes.model.effect;

public record CounterSpellAndPutOnTopOfLibraryEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}
