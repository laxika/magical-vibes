package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Zone;

/** The spell or permanent was cast from the given zone (e.g. hand, graveyard). */
public record CastFromZone(Zone sourceZone) implements Condition {

    @Override
    public String conditionName() {
        return "cast from " + sourceZone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "spell or permanent was not cast from " + sourceZone.name().toLowerCase();
    }
}
