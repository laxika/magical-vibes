package com.github.laxika.magicalvibes.model.effect;

/** Reveals the top planar cards, triggers their chaos abilities, then bottoms them in any order. */
public record RevealTopPlanarCardsAndTriggerChaosEffect(int count) implements CardEffect {
}
