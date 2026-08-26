package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.UUID;

/**
 * Delayed echo trigger for a permanent's next upkeep. The permanent id is resolved at the upkeep
 * so the trigger follows a control change and disappears if the permanent has left the battlefield.
 */
public record EchoAtNextUpkeep(UUID permanentId, String manaCost, DynamicAmount dynamicManaCost,
                               Card sourceCard) implements DelayedAction {

    public EchoAtNextUpkeep(UUID permanentId, String manaCost, Card sourceCard) {
        this(permanentId, manaCost, null, sourceCard);
    }

    public EchoAtNextUpkeep(UUID permanentId, DynamicAmount dynamicManaCost, Card sourceCard) {
        this(permanentId, null, dynamicManaCost, sourceCard);
    }
}
