package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "244")
public class CentaurSafeguard extends Card {

    public CentaurSafeguard() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(new GainLifeEffect(3), "Gain 3 life?"));
    }
}
