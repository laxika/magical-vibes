package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Rest-of-turn trigger for a creature controlled by the registered controller dealing combat damage to a player. */
public record DelayedCombatDamageEffect(
        UUID controllerId,
        Card sourceCard,
        CardEffect triggerEffect,
        UUID sourcePermanentId
) implements DelayedAction {
    public DelayedCombatDamageEffect(UUID controllerId, Card sourceCard, CardEffect triggerEffect) {
        this(controllerId, sourceCard, triggerEffect, null);
    }
}
