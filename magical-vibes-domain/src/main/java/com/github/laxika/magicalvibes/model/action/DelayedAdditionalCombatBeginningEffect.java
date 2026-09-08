package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A delayed trigger created by an effect that adds an additional combat phase. */
public record DelayedAdditionalCombatBeginningEffect(
        UUID controllerId,
        Card sourceCard,
        CardEffect effect
) implements DelayedAction {
}
