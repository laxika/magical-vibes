package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "90")
@CardRegistration(set = "8ED", collectorNumber = "170")
public class VampiricSpirit extends Card {

    public VampiricSpirit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(4));
    }
}
