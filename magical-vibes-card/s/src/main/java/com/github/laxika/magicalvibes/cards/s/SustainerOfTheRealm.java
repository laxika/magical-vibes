package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "52")
@CardRegistration(set = "ULG", collectorNumber = "23")
@CardRegistration(set = "DDC", collectorNumber = "8")
@CardRegistration(set = "DVD", collectorNumber = "8")
@CardRegistration(set = "IMA", collectorNumber = "35")
public class SustainerOfTheRealm extends Card {

    public SustainerOfTheRealm() {
        // Flying comes from Scryfall keywords.
        // Whenever this creature blocks, it gets +0/+2 until end of turn.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(0, 2));
    }
}
