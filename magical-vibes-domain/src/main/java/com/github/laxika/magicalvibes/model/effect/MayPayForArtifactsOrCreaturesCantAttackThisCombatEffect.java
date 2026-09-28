package com.github.laxika.magicalvibes.model.effect;

/**
 * A player may pay one generic mana for each artifact they control; if they do not, creatures
 * cannot attack during the current combat.
 */
public record MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect() implements CardEffect {
}
