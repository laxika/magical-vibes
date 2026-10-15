package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "UDS", collectorNumber = "36")
public class Kingfisher extends Card {

    public Kingfisher() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
