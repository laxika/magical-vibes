package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "79")
public class FangrenMarauder extends Card {

    public FangrenMarauder() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new MayEffect(new GainLifeEffect(5), "Gain 5 life?"));
    }
}
