package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever this creature attacks, defending player mills {@code count} cards" (Nemesis of Reason).
 * <p>
 * Placed on the {@code ON_ATTACK} slot. Non-targeting: the player being attacked (directly, or the
 * controller of the attacked planeswalker) is captured as the trigger's {@code attackedTargetId} by
 * {@code CombatAttackService} and mills {@code count} cards on resolution. Because the defending
 * player is determined by combat rather than chosen, this contributes no player target and never
 * prompts the controller to pick one — unlike a {@link MillEffect} with
 * {@link MillRecipient#TARGET_PLAYER}.
 */
public record MillDefendingPlayerEffect(int count) implements CardEffect {
}
