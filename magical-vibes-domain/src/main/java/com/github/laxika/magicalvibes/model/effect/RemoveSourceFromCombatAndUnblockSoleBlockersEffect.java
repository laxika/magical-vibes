package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the source permanent from combat, makes attackers it was the sole blocker for
 * unblocked, and prevents the source from blocking for the rest of the turn.
 */
public record RemoveSourceFromCombatAndUnblockSoleBlockersEffect() implements CardEffect {
}
