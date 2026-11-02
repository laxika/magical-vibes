package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "106")
public class SlinkingGiant extends Card {

    public SlinkingGiant() {
        // Whenever this creature blocks or becomes blocked, it gets -3/-0 until end of turn.
        addTrigger(Triggers.blocks(), new BoostSelfEffect(-3, 0));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(-3, 0));
    }
}
