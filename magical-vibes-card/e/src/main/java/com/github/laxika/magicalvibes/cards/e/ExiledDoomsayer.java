package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ModifyMorphCostEffect;

@CardRegistration(set = "SCG", collectorNumber = "13")
public class ExiledDoomsayer extends Card {

    public ExiledDoomsayer() {
        addEffect(EffectSlot.STATIC, new ModifyMorphCostEffect(2, CostModificationScope.ALL));
    }
}
