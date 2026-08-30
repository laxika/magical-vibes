package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** One-shot delayed trigger for untapping a player's creatures when that player next attacks. */
public record DelayedAttackUntap(UUID controllerId, Card sourceCard) implements DelayedAction {
}
