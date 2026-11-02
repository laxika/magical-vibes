package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "195")
@CardRegistration(set = "MB1", collectorNumber = "195")
@CardRegistration(set = "TSR", collectorNumber = "397")
public class Leveler extends Card {

    public Leveler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileControllerLibraryEffect());
    }
}
