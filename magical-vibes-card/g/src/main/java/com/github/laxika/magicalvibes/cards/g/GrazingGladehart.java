package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "163")
@CardRegistration(set = "DDH", collectorNumber = "11")
@CardRegistration(set = "DDO", collectorNumber = "47")
@CardRegistration(set = "DDP", collectorNumber = "14")
@CardRegistration(set = "C13", collectorNumber = "147")
public class GrazingGladehart extends Card {

    public GrazingGladehart() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayEffect(new GainLifeEffect(2), "Gain 2 life?"));
    }
}
