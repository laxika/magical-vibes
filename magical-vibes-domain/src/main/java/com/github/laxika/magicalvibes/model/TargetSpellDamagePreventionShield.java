package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Turn-scoped prevention applied to damage from a spell on the stack.
 *
 * @param spellCardId      the prevented spell's card ID
 * @param lifeGainPlayerId the player who gains life for damage prevented, or {@code null}
 */
public record TargetSpellDamagePreventionShield(
        UUID spellCardId,
        UUID lifeGainPlayerId
) {
}
