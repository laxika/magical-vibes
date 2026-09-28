package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayWhileExiledEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceNonHandSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "OTC", collectorNumber = "33")
@CardRegistration(set = "OTC", collectorNumber = "69")
public class SavvyTrader extends Card {

    public SavvyTrader() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCardFromGraveyardMayPlayWhileExiledEffect(
                        new CardIsPermanentPredicate(), true));
        addEffect(EffectSlot.STATIC, new ReduceNonHandSpellCastCostEffect(1));
    }
}
