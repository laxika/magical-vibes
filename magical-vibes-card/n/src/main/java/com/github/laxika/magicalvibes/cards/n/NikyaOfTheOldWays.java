package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "193")
public class NikyaOfTheOldWays extends Card {

    public NikyaOfTheOldWays() {
        addStaticEffect(new NoncreatureSpellsCantBeCastEffect(0, false, false));
        addTrigger(Triggers.playerTapsLandForMana(), new AddOneOfEachManaTypeProducedByLandEffect(true));
    }
}
