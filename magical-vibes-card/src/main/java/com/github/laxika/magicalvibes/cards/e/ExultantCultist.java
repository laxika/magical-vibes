package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "EMN", collectorNumber = "59")
public class ExultantCultist extends Card {

    public ExultantCultist() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
