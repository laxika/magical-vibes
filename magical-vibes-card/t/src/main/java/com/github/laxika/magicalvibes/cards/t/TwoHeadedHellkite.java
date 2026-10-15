package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DMC", collectorNumber = "14")
@CardRegistration(set = "DMC", collectorNumber = "90")
public class TwoHeadedHellkite extends Card {

    public TwoHeadedHellkite() {
        addEffect(EffectSlot.ON_ATTACK, new DrawCardEffect(2));
    }
}
