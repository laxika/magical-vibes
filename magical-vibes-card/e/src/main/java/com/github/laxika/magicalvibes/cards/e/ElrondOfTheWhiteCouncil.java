package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ElrondOfTheWhiteCouncilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "51")
@CardRegistration(set = "LTC", collectorNumber = "134")
@CardRegistration(set = "LTC", collectorNumber = "454")
public class ElrondOfTheWhiteCouncil extends Card {

    public ElrondOfTheWhiteCouncil() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ElrondOfTheWhiteCouncilEffect());
    }
}
