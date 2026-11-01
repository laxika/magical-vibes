package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "59")
@CardRegistration(set = "SIR", collectorNumber = "68")
public class ExultantCultist extends Card {

    public ExultantCultist() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
