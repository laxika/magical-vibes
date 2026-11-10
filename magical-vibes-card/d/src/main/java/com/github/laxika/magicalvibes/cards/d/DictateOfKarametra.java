package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "121")
public class DictateOfKarametra extends Card {

    public DictateOfKarametra() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddOneOfEachManaTypeProducedByLandEffect());
    }
}
