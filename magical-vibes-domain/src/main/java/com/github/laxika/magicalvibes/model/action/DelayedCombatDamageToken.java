package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/** Delayed trigger that creates a token for each qualifying combat-damage source. */
public record DelayedCombatDamageToken(
        UUID controllerId,
        UUID targetPlayerId,
        CreateTokenEffect tokenEffect,
        Card sourceCard
) implements DelayedAction {
}
