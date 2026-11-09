package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A one-shot delayed triggered ability: "when [source] leaves the battlefield or becomes untapped, [effect] on that
 * permanent" (Giant Oyster). It belongs to the player who controlled the ability that created it (CR 603.7e), triggers
 * the next time the source untaps or leaves the battlefield, even if the source was already untapped when it was
 * created (CR 603.7a), and its entry aims {@code effect} at the affected permanent without targeting it.
 */
public record DelayedSourceUntapsOrLeavesTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                                 UUID affectedPermanentId, CardEffect effect)
        implements DelayedAction {
}
