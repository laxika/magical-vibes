package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "120")
@CardRegistration(set = "MM3", collectorNumber = "129")
public class HungrySpriggan extends Card {

    public HungrySpriggan() {
        // Trample (keyword auto-loaded from Scryfall)
        // Whenever Hungry Spriggan attacks, it gets +3/+3 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(3, 3));
    }
}
