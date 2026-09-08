package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger marker for an Aura that exiles cards from the owner's library and grants its
 * controller permission to cast the exiled nonland cards while they remain exiled.
 *
 * <p>The no-argument form is used by the card definition. The death trigger collector binds the
 * dying creature's last-known power and the library owner's ID before putting the effect on the
 * stack.
 */
public record ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect(int count, UUID libraryOwnerId)
        implements CardEffect {

    public ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect() {
        this(0, null);
    }
}
