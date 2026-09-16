package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "MUL", collectorNumber = "46")
@CardRegistration(set = "MUL", collectorNumber = "111")
@CardRegistration(set = "MUL", collectorNumber = "176")
public class JuriMasterOfTheRevue extends Card {

    public JuriMasterOfTheRevue() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_DEATH,
                new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
