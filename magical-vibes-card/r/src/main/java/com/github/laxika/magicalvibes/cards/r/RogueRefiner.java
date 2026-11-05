package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "135")
@CardRegistration(set = "KLR", collectorNumber = "206")
@CardRegistration(set = "DRC", collectorNumber = "118")
public class RogueRefiner extends Card {

    public RogueRefiner() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1), new EnergyCountersEffect(2));
    }
}
