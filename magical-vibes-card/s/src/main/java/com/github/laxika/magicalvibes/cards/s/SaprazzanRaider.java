package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "102")
public class SaprazzanRaider extends Card {

    public SaprazzanRaider() {
        addTrigger(Triggers.becomesBlocked(), ReturnToHandEffect.self());
    }
}
