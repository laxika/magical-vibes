package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Delayed trigger that untaps up to N permanents matching a filter at the beginning of the next end step
 *  (e.g. Teferi, Hero of Dominaria). */
public record DelayedUntapPermanents(UUID controllerId, int count, PermanentPredicate filter, Card sourceCard) implements DelayedAction {
}
