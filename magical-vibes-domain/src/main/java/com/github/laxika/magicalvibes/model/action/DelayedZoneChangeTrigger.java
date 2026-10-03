package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.Map;
import java.util.UUID;

/** A one-shot delayed ability watching a permanent leave for one of the specified zones. */
public record DelayedZoneChangeTrigger(UUID watchedPermanentId, UUID controllerId, Card sourceCard,
                                       Map<Zone, CardEffect> effectsByDestination) implements DelayedAction {
    public DelayedZoneChangeTrigger {
        effectsByDestination = Map.copyOf(effectsByDestination);
    }
}
