package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "FRF", collectorNumber = "159")
public class GoblinBoomKeg extends Card {

    public GoblinBoomKeg() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeSelfEffect());
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(3));
    }
}
