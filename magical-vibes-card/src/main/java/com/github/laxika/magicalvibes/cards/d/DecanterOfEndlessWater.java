package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "CLB", collectorNumber = "309")
public class DecanterOfEndlessWater extends Card {

    public DecanterOfEndlessWater() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
