package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A restriction on an additional combat phase an effect added ("only land creatures can attack during that combat",
 * Bumi Unleashed; "only that creature can attack", Throat Wolf), applied as that combat begins. It is a rules change,
 * not a triggered ability, so it is not a delayed triggered ability; what the added combat triggers
 * ("at the beginning of that combat, untap all creatures") is one, about that combat phase alone.
 */
public record DelayedAdditionalCombatBeginningEffect(
        UUID controllerId,
        Card sourceCard,
        CardEffect effect,
        UUID sourcePermanentId
) implements DelayedAction {

    public DelayedAdditionalCombatBeginningEffect(UUID controllerId, Card sourceCard, CardEffect effect) {
        this(controllerId, sourceCard, effect, null);
    }
}
