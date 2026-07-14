package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of the spell targeted by this stack entry draws a card. Reads the spell from
 * {@code entry.getTargetId()} on the stack, so it must resolve while that spell is still on the
 * stack (i.e. before any accompanying counter). Not a chosen player target — never contributes a
 * player target. Used by Dream Fracture ("Counter target spell. Its controller draws a card.").
 */
public record TargetSpellControllerDrawsCardEffect() implements CardEffect {
}
