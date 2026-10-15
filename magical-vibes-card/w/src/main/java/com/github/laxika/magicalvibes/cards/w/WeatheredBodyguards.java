package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;

@CardRegistration(set = "TSP", collectorNumber = "46")
public class WeatheredBodyguards extends Card {

    public WeatheredBodyguards() {
        addEffect(EffectSlot.STATIC, new RedirectUnblockedCombatDamageToSelfEffect());
    }
}
