package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the permanent that granted or created the triggered ability this effect is part of ("convert Optimus
 * Prime" in the delayed triggered ability Optimus Prime's attack ability creates for the chosen creature,
 * {@code RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn}), the stack entry's granting
 * permanent ({@code StackEntry.getGrantingPermanentId()}).
 */
public record TransformGrantingPermanentEffect() implements CardEffect {
}
