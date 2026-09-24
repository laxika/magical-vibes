package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenEnteredThisTurnEffect;

@CardRegistration(set = "SOC", collectorNumber = "251")
public class RedoubledStormsinger extends Card {

    public RedoubledStormsinger() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenCopyOfEachTokenEnteredThisTurnEffect());
    }
}
