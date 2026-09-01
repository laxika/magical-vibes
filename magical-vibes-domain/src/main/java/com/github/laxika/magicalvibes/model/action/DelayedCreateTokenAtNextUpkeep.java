package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/** Delayed trigger that creates token(s) at the beginning of the controller's next upkeep. */
public record DelayedCreateTokenAtNextUpkeep(UUID controllerId, CreateTokenEffect tokenEffect,
                                             Card sourceCard) implements DelayedAction {
}
