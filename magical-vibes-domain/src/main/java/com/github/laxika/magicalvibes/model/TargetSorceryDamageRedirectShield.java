package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Turn-scoped redirection for damage dealt by one sorcery spell.
 *
 * @param sorceryCardId the sorcery spell whose damage is redirected
 * @param controllerId the controller of that sorcery, who receives the redirected damage
 */
public record TargetSorceryDamageRedirectShield(
        UUID sorceryCardId,
        UUID controllerId
) {
}
