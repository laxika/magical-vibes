package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;

@CardRegistration(set = "ONE", collectorNumber = "47")
public class EncroachingMycosynth extends Card {

    public EncroachingMycosynth() {
        addEffect(EffectSlot.STATIC, new GrantCardTypeToOwnNonlandPermanentsEffect(CardType.ARTIFACT));
    }
}
