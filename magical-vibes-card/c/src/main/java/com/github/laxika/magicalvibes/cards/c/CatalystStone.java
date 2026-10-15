package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ModifyFlashbackCostEffect;

@CardRegistration(set = "ODY", collectorNumber = "297")
public class CatalystStone extends Card {

    public CatalystStone() {
        addEffect(EffectSlot.STATIC, new ModifyFlashbackCostEffect(-2, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new ModifyFlashbackCostEffect(2, CostModificationScope.OPPONENT));
    }
}
