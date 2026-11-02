package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayXManaRevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "75")
public class HollowSpecter extends Card {

    public HollowSpecter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PayXManaRevealCardsChooseOneToDiscardEffect());
    }
}
