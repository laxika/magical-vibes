package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "130")
@CardRegistration(set = "2XM", collectorNumber = "206")
@CardRegistration(set = "DMC", collectorNumber = "159")
public class MaelstromNexus extends Card {

    public MaelstromNexus() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(1).grantedToTheSpell(), new CascadeEffect());
    }
}
