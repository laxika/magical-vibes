package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "202")
@CardRegistration(set = "DSC", collectorNumber = "199")
public class SkolaGrovedancer extends Card {

    public SkolaGrovedancer() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{2}{G}: Mill a card."
        ));
    }
}
