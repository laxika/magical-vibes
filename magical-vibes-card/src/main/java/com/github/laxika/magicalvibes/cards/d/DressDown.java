package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "MH2", collectorNumber = "39")
public class DressDown extends Card {

    public DressDown() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());
    }
}
