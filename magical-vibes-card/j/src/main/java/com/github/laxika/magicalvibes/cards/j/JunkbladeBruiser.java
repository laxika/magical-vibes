package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "220")
public class JunkbladeBruiser extends Card {

    public JunkbladeBruiser() {
        addTrigger(Triggers.youCastSpell().expend(4), new BoostSelfEffect(2, 1));
    }
}
