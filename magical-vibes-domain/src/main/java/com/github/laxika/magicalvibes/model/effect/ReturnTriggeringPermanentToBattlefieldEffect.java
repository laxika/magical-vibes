package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * "Return that card to the battlefield under your control" (Athreos, Shroud-Veiled): returns the card of the
 * permanent that caused the trigger from the zone it was put into, a graveyard or exile, under the ability
 * controller's control. Which permanents count ("a creature with a coin counter on it") is the trigger's filter.
 *
 * @param fromZone the zone the card was put into: {@link Zone#GRAVEYARD} or {@link Zone#EXILE}
 */
public record ReturnTriggeringPermanentToBattlefieldEffect(Zone fromZone) implements CardEffect {
    public ReturnTriggeringPermanentToBattlefieldEffect {
        if (fromZone != Zone.GRAVEYARD && fromZone != Zone.EXILE) {
            throw new IllegalArgumentException("fromZone must be GRAVEYARD or EXILE");
        }
    }
}
