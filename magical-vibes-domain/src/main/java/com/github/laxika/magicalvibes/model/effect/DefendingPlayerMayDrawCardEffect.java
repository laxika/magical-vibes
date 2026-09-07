package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever this creature attacks, defending player may draw a card" (Sibilant Spirit).
 * <p>
 * Placed on the {@code ON_ATTACK} slot. The attacking creature's controller controls the trigger.
 * At resolution, the attacked player, attacked planeswalker's current controller, or attacked
 * battle's protector is offered the draw, rather than the trigger's controller.
 */
public record DefendingPlayerMayDrawCardEffect() implements CardEffect {
}
