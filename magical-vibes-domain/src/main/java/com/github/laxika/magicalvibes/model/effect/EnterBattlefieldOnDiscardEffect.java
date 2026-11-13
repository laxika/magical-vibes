package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "If a spell or ability an opponent controls causes you to discard this card, put it onto the battlefield instead of
 * putting it into your graveyard" (Obstinate Baloth): a replacement effect of the card's static ability, with the
 * counters it enters with, if any.
 */
public record EnterBattlefieldOnDiscardEffect(CounterType counterType, int counterCount) implements CardEffect {

    public EnterBattlefieldOnDiscardEffect() {
        this(null, 0);
    }

    /** It moves the card out of its owner's hand, so it functions only there (CR 113.6m). */
    @Override
    public AbilityZones staticAbilityZones() {
        return AbilityZones.HAND;
    }
}
