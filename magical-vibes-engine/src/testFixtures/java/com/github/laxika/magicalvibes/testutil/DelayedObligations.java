package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.UUID;

/** Delayed triggered abilities that a player can stop by paying, built as the cards that make them build them. */
public final class DelayedObligations {

    private DelayedObligations() {
    }

    /**
     * Nafs Asp's delayed ability of {@code controllerId} (the ability's controller): {@code playerId} loses
     * {@code lifeLoss} life at the beginning of their next draw step unless they pay {@code {payAmount}} before it.
     */
    public static TemporaryGlobalTriggeredAbility loseLifeAtDrawStepUnlessPaid(UUID controllerId, UUID playerId,
                                                                               Card source, int lifeLoss,
                                                                               int payAmount) {
        return TemporaryGlobalTriggeredAbility.atNextStepUnlessPaid(controllerId, source,
                new TriggeredAbility(Triggers.drawStep(ActivePlayer.CHOSEN_PLAYER),
                        new LoseLifeEffect(lifeLoss, Subjects.triggeringPlayer())),
                null, null, playerId, "{" + payAmount + "}");
    }
}
