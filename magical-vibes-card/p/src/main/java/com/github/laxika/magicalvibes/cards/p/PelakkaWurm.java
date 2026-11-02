package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "192")
@CardRegistration(set = "ROE", collectorNumber = "204")
@CardRegistration(set = "MM2", collectorNumber = "154")
public class PelakkaWurm extends Card {

    public PelakkaWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(7));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
