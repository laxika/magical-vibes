package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

/** Delayed trigger that creates token(s) at the beginning of the next end step (e.g. Rukh Egg). */
public record DelayedCreateToken(UUID controllerId, CreateTokenEffect tokenEffect, Card sourceCard) implements DelayedAction {
}
