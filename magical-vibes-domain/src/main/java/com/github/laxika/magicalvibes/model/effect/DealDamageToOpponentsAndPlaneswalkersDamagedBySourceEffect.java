package com.github.laxika.magicalvibes.model.effect;

/** Deals damage to the current opponents and planeswalkers previously damaged by the source. */
public record DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect(int damage) implements CardEffect {
}
