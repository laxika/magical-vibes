package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Zone;

/** The source permanent entered the battlefield from the given zone. */
public record EnteredFromZone(Zone sourceZone) implements Condition {

    @Override
    public String conditionName() {
        return "entered from " + sourceZone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "permanent did not enter from " + sourceZone.name().toLowerCase();
    }
}
