package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "10E", collectorNumber = "167")
public class PhyrexianRager extends Card {

    public PhyrexianRager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1));
    }
}
