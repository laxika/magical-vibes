package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSameTotalPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "144")
@CardRegistration(set = "H09", collectorNumber = "30")
public class WildPair extends Card {

    public WildPair() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new SearchLibraryForCreatureWithSameTotalPowerToughnessEffect());
    }
}
