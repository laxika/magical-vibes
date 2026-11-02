package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "156")
public class Headhunter extends Card {

    public Headhunter() {
        addMorph("{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
