package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever this creature attacks, defending player may draw a card" (Sibilant Spirit).
 * <p>
 * Placed on the {@code ON_ATTACK} slot. {@code CombatAttackService} routes the optional draw to the
 * defending player (the player being attacked, directly or via one of their planeswalkers) rather
 * than the source's controller, unlike the generic {@code MayEffect} handler which offers the draw
 * to the attacking creature's controller.
 */
public record DefendingPlayerMayDrawCardEffect() implements CardEffect {
}
