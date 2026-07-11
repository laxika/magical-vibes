package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability (e.g. Cunning Giant): "If this creature is unblocked, you may have it assign
 * its combat damage to a creature defending player controls." When the creature is unblocked
 * and the defending player controls at least one creature, the attacking player is prompted to
 * assign all of the creature's combat damage either to the defending player (the default) or to
 * a single defending creature.
 */
public record AssignCombatDamageToDefendingCreatureWhenUnblockedEffect() implements CardEffect {
}
