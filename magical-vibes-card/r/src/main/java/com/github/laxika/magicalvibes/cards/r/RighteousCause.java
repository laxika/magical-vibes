package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "51")
@CardRegistration(set = "DDC", collectorNumber = "22")
@CardRegistration(set = "DVD", collectorNumber = "22")
@CardRegistration(set = "CMD", collectorNumber = "29")
@CardRegistration(set = "CMA", collectorNumber = "21")
public class RighteousCause extends Card {

    public RighteousCause() {
        addTrigger(Triggers.creatureAttacks(), new GainLifeEffect(1));
    }
}
