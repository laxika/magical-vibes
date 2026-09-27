package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaValueParity;

/** True when the source permanent's effective power has the requested parity. */
public record SourcePowerParity(ManaValueParity parity) implements Condition {

    @Override
    public String conditionName() {
        return "source power is " + parity.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "source power is not " + parity.name().toLowerCase();
    }
}
