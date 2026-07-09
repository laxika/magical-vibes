package com.github.laxika.magicalvibes.model.effect;

/**
 * Ashling the Pilgrim's activated ability resolution: put a +1/+1 counter on Ashling, then — if
 * this is the third time this ability has resolved this turn — remove all +1/+1 counters from
 * Ashling and have it deal that much damage to each creature and each player.
 *
 * <p>Counting is per source permanent (not per name) and by resolutions, not activations, so
 * copies of the ability count but abilities still on the stack or from other creatures do not
 * (CR-consistent with the card's rulings). The bonus fires only on the exact third resolution.
 */
public record AshlingThePilgrimEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }

    @Override
    public boolean isDamageOrDestruction() { return true; }
}
