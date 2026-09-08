package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Redirects damage that would be dealt to creatures controlled by a player onto one chosen
 * creature until that player's next turn.
 *
 * @param protectedPlayerId the player whose creatures are protected
 * @param redirectTargetCreatureId the creature that receives redirected damage
 */
public record CreatureControllerDamageRedirectShield(
        UUID protectedPlayerId,
        UUID redirectTargetCreatureId
) {
}
