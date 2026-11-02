package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RagingRiverEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2ED", collectorNumber = "169")
@CardRegistration(set = "LEA", collectorNumber = "168")
@CardRegistration(set = "LEB", collectorNumber = "169")
public class RagingRiver extends Card {

    public RagingRiver() {
        addTrigger(Triggers.youAttack(), new RagingRiverEffect());
    }
}
