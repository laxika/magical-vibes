package com.github.laxika.magicalvibes.model.effect;

/**
 * "Pay X life. Draw X cards." On resolution the controller chooses X (capped at their current
 * life total, since a player can't pay more life than they have), pays that much life, then draws
 * that many cards. Used by Necrologia, whose "as an additional cost to cast, pay X life / draw X
 * cards" is modeled as a single resolution-time X choice.
 */
public record PayXLifeDrawXCardsEffect() implements CardEffect {
}
