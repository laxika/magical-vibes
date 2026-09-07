package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/** A non-targeting ability scheduled for the next end step, retaining its source and affected object. */
public record DelayedEndStepTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                    UUID affectedPermanentId, CardEffect effect) implements DelayedAction {
}
