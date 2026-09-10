package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;

@CardRegistration(set = "AFR", collectorNumber = "247")
public class IronGolem extends Card {

    public IronGolem() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.STATIC, new MustBlockEachCombatEffect());
    }
}
