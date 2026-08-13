package com.github.laxika.magicalvibes.model.effect;

/**
 * Tribute N: as this creature enters, an opponent chosen by its controller may put N additional
 * +1/+1 counters on it. The choice is handled by the battlefield-entry and may-ability services.
 *
 * @param counterCount the number of additional +1/+1 counters required to pay tribute
 */
public record TributeEffect(int counterCount) implements CardEffect {
}
