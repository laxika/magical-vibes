package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;

@CardRegistration(set = "MBS", collectorNumber = "31")
public class SerumRaker extends Card {

    public SerumRaker() {
        addEffect(EffectSlot.ON_DEATH, new EachPlayerDiscardsEffect(1));
    }
}
