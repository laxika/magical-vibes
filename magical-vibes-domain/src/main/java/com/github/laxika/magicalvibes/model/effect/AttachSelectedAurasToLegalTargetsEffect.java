package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Attaches selected Aura permanents to legal permanents or players as they enter. */
public record AttachSelectedAurasToLegalTargetsEffect(List<UUID> auraPermanentIds)
        implements CardEffect {

    public AttachSelectedAurasToLegalTargetsEffect {
        auraPermanentIds = List.copyOf(auraPermanentIds);
    }
}
