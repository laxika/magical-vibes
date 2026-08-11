package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "INV", collectorNumber = "245")
public class DuelingGrounds extends Card {

    public DuelingGrounds() {
        addEffect(EffectSlot.STATIC, new MaximumCombatCreaturesEffect(1, 1));
    }
}
