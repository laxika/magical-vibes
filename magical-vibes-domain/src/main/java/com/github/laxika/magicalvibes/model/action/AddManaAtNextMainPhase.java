package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;

/** Delayed trigger that, at the beginning of the controller's next main phase, lets them add
 *  {@code amount} mana of {@code color} (Scattering Stroke's clash-win reward). The amount is the
 *  countered spell's mana value, snapshotted when the reward resolved. */
public record AddManaAtNextMainPhase(UUID controllerId, ManaColor color, int amount, Card sourceCard) implements DelayedAction {
}
