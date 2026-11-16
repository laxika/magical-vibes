package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns an Aura card from its owner's graveyard to the battlefield attached to a specific permanent, under the
 * control of {@code returnControllerId}, the player the delayed return ability that carries it out belongs to
 * (CR 110.2a). Used as the resolving effect of a delayed return trigger; the enchanted permanent and the Aura card
 * are both checked when the trigger resolves, so the return does nothing if either has left its zone.
 */
public record ReturnAuraFromGraveyardAttachedToPermanentEffect(UUID auraCardId, UUID returnControllerId,
                                                               UUID enchantedPermanentId) implements CardEffect {
}
