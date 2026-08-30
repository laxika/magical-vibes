package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/** Delayed trigger for looking at a damaged player's hand and drawing a card. */
public record DelayedCombatDamageLookAtHandAndDraw(
        UUID controllerId,
        Card sourceCard,
        PermanentPredicate sourcePredicate
) implements DelayedAction {
}
