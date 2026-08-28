package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "160")
public class NumotTheDevastator extends Card {

    public NumotTheDevastator() {
        target(TargetFilters.land(), 0, 2)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new MayPayManaEffect("{2}{R}", new DestroyEachTargetPermanentEffect(),
                                "Pay {2}{R}?"));
    }
}
