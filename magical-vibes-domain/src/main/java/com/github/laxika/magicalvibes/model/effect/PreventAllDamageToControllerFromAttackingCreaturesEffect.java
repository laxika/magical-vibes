package com.github.laxika.magicalvibes.model.effect;

/**
 * "Prevent all damage that would be dealt to you this turn by attacking creatures." (Deep Wood)
 * Adds the controller to {@code GameData.playersWithDamageFromAttackersPrevented}. Combat damage that
 * attacking creatures deal to that player is fully prevented; noncombat damage is prevented only when
 * its source permanent is currently attacking. Cleared at turn cleanup.
 */
public record PreventAllDamageToControllerFromAttackingCreaturesEffect() implements CardEffect {
}
