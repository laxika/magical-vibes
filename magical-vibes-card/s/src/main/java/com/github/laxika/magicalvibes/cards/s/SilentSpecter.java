package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "169")
public class SilentSpecter extends Card {

    public SilentSpecter() {
        addMorph("{3}{B}{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
