package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed echo trigger for a permanent's next upkeep. The permanent id is resolved at the upkeep
 * so the trigger follows a control change and disappears if the permanent has left the battlefield.
 */
public record EchoAtNextUpkeep(UUID permanentId, String manaCost, Card sourceCard) implements DelayedAction {
}
