package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedTargetGroup;

import java.util.List;
import java.util.UUID;

/** A one-shot ability scheduled for the next end step, retaining its source and affected object. */
public record DelayedEndStepTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                    UUID affectedPermanentId, CardEffect effect,
                                    List<DelayedTargetGroup> targetGroups) implements DelayedAction {

    public DelayedEndStepTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                 UUID affectedPermanentId, CardEffect effect) {
        this(controllerId, sourceCard, sourcePermanentId, affectedPermanentId, effect, List.of());
    }

    public DelayedEndStepTrigger {
        targetGroups = List.copyOf(targetGroups);
    }
}
