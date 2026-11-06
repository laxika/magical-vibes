package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of card types among the cards the resolving trigger's event was about ("for each card type among those
 * cards", Polluted Cistern): the stack entry's triggering cards, as they were in the event. A multi-type card counts
 * each of its types (artifact creature: artifact and creature). 0 when the entry has no triggering cards.
 */
public record CardTypesAmongTriggeringCards() implements DynamicAmount {
}
