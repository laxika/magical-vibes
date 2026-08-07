package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "CHK", collectorNumber = "201")
public class AzusaLostButSeeking extends Card {

    public AzusaLostButSeeking() {
        // You may play two additional lands on each of your turns.
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(2));
    }
}
