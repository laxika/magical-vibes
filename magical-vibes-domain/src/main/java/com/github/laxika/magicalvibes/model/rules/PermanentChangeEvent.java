package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Objects;
import java.util.UUID;

/**
 * A permanent phased, transformed or was turned face up. None of these is a zone change (CR 702.26d).
 *
 * @param change       what happened to it
 * @param permanent    a copy of the permanent: as it was just before it phased out (CR 603.10b), and as it is
 *                     after the change otherwise
 * @param controllerId its controller
 */
public record PermanentChangeEvent(PermanentChange change, Permanent permanent, UUID controllerId)
        implements RulesEvent {

    public PermanentChangeEvent {
        Objects.requireNonNull(change, "change");
        Objects.requireNonNull(permanent, "permanent");
    }

    @Override
    public PermanentChangeEvent deepCopy() {
        return new PermanentChangeEvent(change, new Permanent(permanent), controllerId);
    }
}
