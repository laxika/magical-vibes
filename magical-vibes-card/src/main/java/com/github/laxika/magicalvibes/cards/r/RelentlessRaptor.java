package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;

@CardRegistration(set = "RIX", collectorNumber = "169")
public class RelentlessRaptor extends Card {

    public RelentlessRaptor() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.STATIC, new MustBlockEachCombatEffect());
    }
}
