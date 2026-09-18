package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.UUID;

/**
 * A trigger descriptor that can be bound to the source and target of the resolving ability before
 * it is stored as a temporary global trigger.
 */
public interface TemporaryGlobalTriggerEffect extends CardEffect {

    List<CardEffect> resolvedEffects();

    UUID targetPlayerId();

    boolean matches(Zone sourceZone, UUID exiledSourcePermanentId);

    TemporaryGlobalTriggerEffect bindTo(UUID sourcePermanentId, UUID targetPlayerId);
}
