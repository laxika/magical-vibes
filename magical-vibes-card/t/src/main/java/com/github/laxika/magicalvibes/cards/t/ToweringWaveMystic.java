package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "77")
public class ToweringWaveMystic extends Card {

    public ToweringWaveMystic() {
        // Whenever this creature deals damage, target player mills that many cards.
        addTrigger(Triggers.dealsDamage(),
                new MillEffect(new EventValue(), MillRecipient.TARGET_PLAYER));
    }
}
