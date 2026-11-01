package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPutsCardFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "103")
@CardRegistration(set = "DDM", collectorNumber = "52")
public class SadisticAugermage extends Card {

    public SadisticAugermage() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new EachPlayerPutsCardFromHandOnTopOfLibraryEffect());
    }
}
