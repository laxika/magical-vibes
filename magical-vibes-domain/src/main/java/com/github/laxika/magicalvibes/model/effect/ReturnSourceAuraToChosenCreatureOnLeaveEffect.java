package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns this Aura from its owner's graveyard when the enchanted permanent leaves the
 * battlefield, with that permanent's former controller choosing a legal creature to enchant.
 */
public record ReturnSourceAuraToChosenCreatureOnLeaveEffect(UUID leavingPermanentControllerId)
        implements CardEffect {

    public ReturnSourceAuraToChosenCreatureOnLeaveEffect() {
        this(null);
    }
}
