package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay {X}. If they do, a reflexive triggered ability targets
 * an instant or sorcery card with mana value X from any graveyard for a free cast.
 */
public record PayXManaCastTargetInstantOrSorceryFromGraveyardEffect() implements CardEffect {
}
