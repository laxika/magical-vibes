package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "S99", collectorNumber = "89")
public class ShriekingSpecter extends Card {

    public ShriekingSpecter() {
        addTrigger(Triggers.attacks(),
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
    }
}
