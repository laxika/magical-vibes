package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect on an Aura or Equipment: prevent all combat damage that would be dealt to the
 * creature it is attached to. Noncombat damage is unaffected, and the attached creature still
 * deals its own combat damage (General's Kabuto).
 */
public record PreventAllCombatDamageToAttachedCreatureEffect() implements CardEffect {
}
