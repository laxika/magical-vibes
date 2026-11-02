package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C14", collectorNumber = "44")
@CardRegistration(set = "CMA", collectorNumber = "112")
public class GraveSifter extends Card {

    public GraveSifter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect());
    }
}
