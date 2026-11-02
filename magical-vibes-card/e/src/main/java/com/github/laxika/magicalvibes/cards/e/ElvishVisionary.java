package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "178")
@CardRegistration(set = "ALA", collectorNumber = "130")
@CardRegistration(set = "M13", collectorNumber = "169")
@CardRegistration(set = "ORI", collectorNumber = "175")
@CardRegistration(set = "DPA", collectorNumber = "63")
@CardRegistration(set = "DDR", collectorNumber = "8")
@CardRegistration(set = "SLD", collectorNumber = "763")
@CardRegistration(set = "HA1", collectorNumber = "13")
@CardRegistration(set = "GN3", collectorNumber = "96")
@CardRegistration(set = "C14", collectorNumber = "193")
@CardRegistration(set = "C15", collectorNumber = "182")
@CardRegistration(set = "LTC", collectorNumber = "240")
@CardRegistration(set = "HOC", collectorNumber = "206")
@CardRegistration(set = "CMA", collectorNumber = "105")
public class ElvishVisionary extends Card {

    public ElvishVisionary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
