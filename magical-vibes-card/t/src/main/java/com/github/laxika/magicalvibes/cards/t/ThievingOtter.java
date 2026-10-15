package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IKO", collectorNumber = "69")
public class ThievingOtter extends Card {

    public ThievingOtter() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new DrawCardEffect());
    }
}
