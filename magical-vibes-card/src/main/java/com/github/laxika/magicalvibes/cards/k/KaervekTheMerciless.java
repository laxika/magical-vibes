package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;

@CardRegistration(set = "TSP", collectorNumber = "242")
public class KaervekTheMerciless extends Card {

    public KaervekTheMerciless() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(null));
    }
}
