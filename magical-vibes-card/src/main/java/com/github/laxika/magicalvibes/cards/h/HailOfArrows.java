package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "18")
public class HailOfArrows extends Card {

    public HailOfArrows() {
        addEffect(EffectSlot.SPELL, new DealXDamageDividedAmongTargetAttackingCreaturesEffect());
    }
}
