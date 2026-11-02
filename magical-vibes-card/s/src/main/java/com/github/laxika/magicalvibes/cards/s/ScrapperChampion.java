package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "97")
@CardRegistration(set = "KLR", collectorNumber = "144")
public class ScrapperChampion extends Card {

    public ScrapperChampion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(2));

        addTrigger(Triggers.attacks(), new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(2),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-2),
                                new PutCountersOnSourceEffect(1, 1, 1))),
                "Pay {E}{E} to put a +1/+1 counter on Scrapper Champion?"));
    }
}
