package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/**
 * Delayed trigger that creates token(s) at the beginning of the controller's next upkeep.
 *
 * @param eventValue the event value of the ability that created it ("for each creature returned to your hand this
 *                   way"), the delayed ability's own event value, which an {@code EventValue} token amount reads as it
 *                   resolves
 */
public record DelayedCreateTokenAtNextUpkeep(UUID controllerId, CreateTokenEffect tokenEffect,
                                             Card sourceCard, boolean anyPlayerNextUpkeep, int eventValue)
        implements DelayedAction {

    public DelayedCreateTokenAtNextUpkeep(UUID controllerId, CreateTokenEffect tokenEffect,
                                          Card sourceCard) {
        this(controllerId, tokenEffect, sourceCard, false, 0);
    }
}
