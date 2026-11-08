package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the card the resolving trigger's event was about ("pay life equal to its mana value", Lorcan,
 * Warlock Collector): the stack entry's triggering card, as it was in the event (its triggering cards), else where it
 * is now. 0 when the entry has no triggering card.
 */
public record TriggeringCardManaValue() implements DynamicAmount {
}
