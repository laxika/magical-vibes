package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapCreaturesSharingDyingCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "35")
public class FacesOfThePast extends Card {

    public FacesOfThePast() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new TapOrUntapCreaturesSharingDyingCreatureTypeEffect());
    }
}
