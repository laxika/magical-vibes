package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "5")
@CardRegistration(set = "6ED", collectorNumber = "6")
public class Castle extends Card {

    public Castle() {
        // Untapped creatures you control get +0/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.OWN_UNTAPPED_CREATURES));
    }
}
