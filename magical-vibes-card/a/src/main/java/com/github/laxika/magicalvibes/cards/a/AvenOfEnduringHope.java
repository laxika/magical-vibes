package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "5")
@CardRegistration(set = "AKR", collectorNumber = "6")
public class AvenOfEnduringHope extends Card {

    public AvenOfEnduringHope() {
        // Flying is auto-loaded from Scryfall as a keyword.
        // When this creature enters, you gain 3 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
