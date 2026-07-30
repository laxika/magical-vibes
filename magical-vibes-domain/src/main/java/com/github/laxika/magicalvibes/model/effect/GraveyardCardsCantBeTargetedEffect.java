package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: cards in graveyards can't be the targets of spells or abilities.
 * Used by Ground Seal (M13) and similar effects. Non-targeting graveyard interaction
 * (mass exile, resolution-time "choose a card in your graveyard") is unaffected.
 */
public record GraveyardCardsCantBeTargetedEffect() implements CardEffect {
}
