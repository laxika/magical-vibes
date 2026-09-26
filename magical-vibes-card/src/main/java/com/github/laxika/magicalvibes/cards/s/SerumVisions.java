package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "5DN", collectorNumber = "36")
@CardRegistration(set = "MM3", collectorNumber = "49")
@CardRegistration(set = "SLD", collectorNumber = "29")
@CardRegistration(set = "SLD", collectorNumber = "30")
@CardRegistration(set = "SLD", collectorNumber = "31")
@CardRegistration(set = "SLD", collectorNumber = "32")
@CardRegistration(set = "SLD", collectorNumber = "2323")
@CardRegistration(set = "SLD", collectorNumber = "2338")
public class SerumVisions extends Card {

    public SerumVisions() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
