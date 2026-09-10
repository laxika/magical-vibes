package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

/** Repeating upkeep trigger for Dimensional Breach's exiled permanent cards. */
public record DimensionalBreachUpkeepReturn(Card sourceCard) implements DelayedAction {
}
