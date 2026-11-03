package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "69")
public class MerfolkFalconer extends Card {

    public MerfolkFalconer() {
        addTrigger(Triggers.youCastSpell().kicked(), new ScryEffect(2));
    }
}
