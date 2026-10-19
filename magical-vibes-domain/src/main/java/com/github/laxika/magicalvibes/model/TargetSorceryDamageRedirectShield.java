package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Turn-scoped redirection for damage dealt by one sorcery spell.
 *
 * @param sorceryCardId the sorcery spell whose damage is redirected
 * @param controllerId the controller of that sorcery, who receives the redirected damage
 * @param graveyardEntryVersion the sorcery card's graveyard-entry version when the redirect was set up;
 *                              once that card enters a graveyard again the version changes, so a later
 *                              cast of the same card is a new spell the redirect does not cover
 */
public record TargetSorceryDamageRedirectShield(
        UUID sorceryCardId,
        UUID controllerId,
        long graveyardEntryVersion
) {
}
