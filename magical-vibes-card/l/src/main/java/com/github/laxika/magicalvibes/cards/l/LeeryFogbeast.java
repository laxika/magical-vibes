package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "273")
public class LeeryFogbeast extends Card {

    public LeeryFogbeast() {
        addTrigger(Triggers.becomesBlocked(), PreventDamageEffect.allCombat());
    }
}
