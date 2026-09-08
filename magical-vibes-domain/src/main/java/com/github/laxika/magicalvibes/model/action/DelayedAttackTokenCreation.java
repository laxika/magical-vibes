package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/** Delayed trigger that creates a fixed number of tokens whenever the controller attacks this turn. */
public record DelayedAttackTokenCreation(
        UUID controllerId,
        int amount,
        CreateTokenEffect tokenEffect,
        boolean sacrificeAtEndStep,
        Card sourceCard
) implements DelayedAction {
}
