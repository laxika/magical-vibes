package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

/** Marks the current combat for Camouflage's pile-based blocker declaration. */
public record DelayedCamouflage(Card sourceCard) implements DelayedAction {
}
