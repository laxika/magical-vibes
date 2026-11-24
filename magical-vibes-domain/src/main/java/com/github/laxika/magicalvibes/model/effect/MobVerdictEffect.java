package com.github.laxika.magicalvibes.model.effect;

/**
 * Mob Verdict, after its {@code VoteEffect.forAnotherPlayer().castSecretly()} step: "for each vote an opponent received, Mob
 * Verdict deals 2 damage to that player and each creature that player controls", one damage event per vote, read from
 * the resolution's VOTE step. The draw for each vote you received is its own step.
 */
public record MobVerdictEffect() implements CardEffect {
}
