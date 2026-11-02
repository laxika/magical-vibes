package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "22")
public class IgnobleSoldier extends Card {

    public IgnobleSoldier() {
        addTrigger(Triggers.becomesBlocked(), PreventDamageEffect.allCombatBySelf());
    }
}
