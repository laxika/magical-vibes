package com.github.laxika.magicalvibes.model.effect;

/** Resolves Danse Macabre's sacrifice phase or one of its return branches. */
public record DanseMacabreEffect(int maxReturnCount, boolean mandatoryReturn) implements CardEffect {

    public DanseMacabreEffect() {
        this(0, false);
    }

    public DanseMacabreEffect {
        if (maxReturnCount < 0 || maxReturnCount > 2) {
            throw new IllegalArgumentException("Danse Macabre can return at most two cards");
        }
        if (mandatoryReturn && maxReturnCount != 1) {
            throw new IllegalArgumentException("The one-card Danse Macabre branch must be mandatory");
        }
    }

    public boolean sacrificePhase() {
        return maxReturnCount == 0;
    }
}
