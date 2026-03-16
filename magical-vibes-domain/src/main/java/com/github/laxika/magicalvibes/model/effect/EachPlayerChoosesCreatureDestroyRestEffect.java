package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature they control. Destroy the rest.
 *
 * <p>Uses APNAP ordering: active player chooses first, then each other player in turn order.
 * After all choices are made, all non-chosen creatures are destroyed simultaneously.
 * Respects indestructible and regeneration.
 */
public record EachPlayerChoosesCreatureDestroyRestEffect() implements CardEffect {
}
