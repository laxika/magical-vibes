package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "USG", collectorNumber = "250")
public class Exploration extends Card {

    public Exploration() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
    }
}
