package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A repeating delayed triggered ability: "for as long as [source] remains tapped, at the beginning of each of your
 * draw steps, [effect] on that permanent" (Giant Oyster). It belongs to the player who controlled the ability that
 * created it (CR 603.7e), triggers at the beginning of each of that player's draw steps, and ends once the source
 * untaps or leaves the battlefield ({@code sourceUntapSequence} is the source's untap count when it was created).
 * Its entry aims {@code effect} at the affected permanent without targeting it.
 */
public record DelayedDrawStepTriggerWhileSourceTapped(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                                     int sourceUntapSequence, UUID affectedPermanentId,
                                                     CardEffect effect) implements DelayedAction {
}
