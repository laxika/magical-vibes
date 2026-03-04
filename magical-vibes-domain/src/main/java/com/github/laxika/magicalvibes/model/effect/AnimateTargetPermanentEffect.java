package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes a target permanent into a creature with base power and toughness permanently
 * (no "until end of turn" duration). The permanent retains its other types.
 * Used by Tezzeret, Agent of Bolas -1 and similar effects.
 */
public record AnimateTargetPermanentEffect(int power, int toughness) implements CardEffect {

    @Override
    public boolean canTargetPermanent() { return true; }
}
