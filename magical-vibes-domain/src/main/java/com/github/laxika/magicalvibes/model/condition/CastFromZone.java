package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * The spell or permanent was cast from the given zone (e.g. hand, graveyard). {@code fromControllersZone}
 * additionally requires that zone to be the controller's own (a card is only ever in its owner's graveyard
 * or library, so this checks the card's owner).
 */
public record CastFromZone(Zone sourceZone, boolean fromControllersZone) implements Condition {

    public CastFromZone(Zone sourceZone) {
        this(sourceZone, false);
    }

    @Override
    public String conditionName() {
        return "cast from " + sourceZone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "spell or permanent was not cast from " + sourceZone.name().toLowerCase();
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
