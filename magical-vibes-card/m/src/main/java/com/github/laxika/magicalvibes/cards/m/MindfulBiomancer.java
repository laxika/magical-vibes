package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "154")
public class MindfulBiomancer extends Card {

    public MindfulBiomancer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1));
    }
}
