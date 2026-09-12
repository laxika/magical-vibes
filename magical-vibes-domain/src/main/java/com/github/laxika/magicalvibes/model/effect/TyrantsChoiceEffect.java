package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for death or torture. Death wins only
 * with a strict majority; otherwise each opponent loses 4 life.
 */
public record TyrantsChoiceEffect() implements CardEffect {
}
