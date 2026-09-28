package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the supplied turn-face-up trigger and a cost reduction to each eligible face-down
 * creature controlled by the resolving player.
 */
public record GrantTurnFaceUpAbilityToOwnFaceDownCreaturesEffect(
        int turnFaceUpCostReduction, CardEffect turnFaceUpEffect) implements CardEffect {
}
