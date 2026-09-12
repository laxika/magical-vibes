package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/** An ability that triggers at the beginning of the next cleanup step. */
public record DelayedCleanupTrigger(UUID controllerId, Card sourceCard, CardEffect effect)
        implements DelayedAction {
}
