package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that the controller chooses a creature
 * on the battlefield — any creature, regardless of controller — as this permanent enters
 * ("As this Aura enters, choose a creature."). The chosen creature's ID is stored on the entering
 * permanent via {@code chosenPermanentId}.
 *
 * <p>Unlike {@link ChooseAnotherCreatureOnEnterEffect} the choice is not restricted to other
 * creatures the controller controls, and it is supported for Auras (which are not creatures
 * themselves), not just entering creatures. Used by Metamorphic Alteration.
 */
public record ChooseCreatureOnEnterEffect() implements CardEffect {
}
