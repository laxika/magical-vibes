package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "22")
public class MajesticAuricorn extends Card {

    public MajesticAuricorn() {
        addTrigger(Triggers.mutates(), new GainLifeEffect(4));
    }
}
