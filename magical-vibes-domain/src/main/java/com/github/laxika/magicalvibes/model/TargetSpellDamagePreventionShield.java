package com.github.laxika.magicalvibes.model;

import java.util.Set;
import java.util.UUID;

/**
 * Turn-scoped prevention applied to damage from a chosen spell or permanent incarnation.
 *
 * @param spellCardId the prevented spell's card ID, or {@code null}
 * @param lifeGainPlayerId the player who gains life for damage prevented, or {@code null}
 * @param sourcePermanentId the chosen permanent incarnation, or {@code null}
 * @param requiredColors colors the source must still have when it deals damage; empty allows any color
 */
public record TargetSpellDamagePreventionShield(
        UUID spellCardId,
        UUID lifeGainPlayerId,
        UUID sourcePermanentId,
        Set<CardColor> requiredColors
) {
    public TargetSpellDamagePreventionShield {
        requiredColors = Set.copyOf(requiredColors);
    }

    public TargetSpellDamagePreventionShield(UUID spellCardId, UUID lifeGainPlayerId) {
        this(spellCardId, lifeGainPlayerId, null, Set.of());
    }
}
