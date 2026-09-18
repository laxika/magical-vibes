package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/** Delayed trigger watching one chosen creature attack one of the registering player's opponents. */
public record DelayedWatchedCreatureAttack(
        UUID watchedPermanentId,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard
) implements DelayedAction {
}
