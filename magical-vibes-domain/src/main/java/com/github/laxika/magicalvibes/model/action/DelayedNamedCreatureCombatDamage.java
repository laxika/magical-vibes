package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * Delayed trigger for creatures with a chosen name dealing combat damage to a player.
 */
public record DelayedNamedCreatureCombatDamage(
        String cardName,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard,
        boolean combatDamageToPlayerOnly,
        boolean untilEndOfTurn
) implements DelayedAction {

    public DelayedNamedCreatureCombatDamage {
        effects = List.copyOf(effects);
    }
}
