package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever a creature dies, that creature's controller may draw a card" (Fecundity).
 * <p>
 * Placed on the {@code ON_ANY_CREATURE_DIES} slot. The trigger collector routes the optional draw
 * to the dying creature's controller (who may be an opponent of this permanent's controller),
 * unlike the generic {@code MayEffect} handler which offers the draw to the source's controller.
 */
public record DyingCreatureControllerMayDrawCardEffect() implements CardEffect {
}
