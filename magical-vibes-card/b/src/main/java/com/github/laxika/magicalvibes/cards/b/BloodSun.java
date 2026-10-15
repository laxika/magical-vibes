package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllNonManaAbilitiesEffect;

@CardRegistration(set = "RIX", collectorNumber = "92")
public class BloodSun extends Card {

    public BloodSun() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addEffect(EffectSlot.STATIC, new LosesAllNonManaAbilitiesEffect(GrantScope.ALL_LANDS));
    }
}
