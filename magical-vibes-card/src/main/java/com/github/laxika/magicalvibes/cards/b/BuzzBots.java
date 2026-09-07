package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TMT", collectorNumber = "32")
public class BuzzBots extends Card {

    public BuzzBots() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));
    }
}
