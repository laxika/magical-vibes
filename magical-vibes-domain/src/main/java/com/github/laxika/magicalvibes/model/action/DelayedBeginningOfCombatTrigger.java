package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A one-shot trigger waiting for the next beginning of combat this turn. */
public record DelayedBeginningOfCombatTrigger(
        UUID controllerId,
        Card sourceCard,
        CardEffect effect
) implements DelayedAction {
}
