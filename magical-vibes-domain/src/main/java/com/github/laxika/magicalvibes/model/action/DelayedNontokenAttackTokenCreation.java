package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/** Delayed trigger that counts nontoken creatures attacking during the current turn. */
public record DelayedNontokenAttackTokenCreation(
        UUID controllerId,
        CreateTokenEffect tokenEffect,
        Card sourceCard
) implements DelayedAction {
}
