package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "21")
public class RadiantsDragoons extends Card {

    public RadiantsDragoons() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));
        addStaticEffect(new EchoEffect("{3}{W}"));
    }
}
