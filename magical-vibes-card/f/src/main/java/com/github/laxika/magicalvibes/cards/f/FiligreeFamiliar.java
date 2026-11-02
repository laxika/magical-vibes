package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "212")
@CardRegistration(set = "KLR", collectorNumber = "239")
@CardRegistration(set = "DDU", collectorNumber = "53")
@CardRegistration(set = "GNT", collectorNumber = "52")
public class FiligreeFamiliar extends Card {

    public FiligreeFamiliar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
