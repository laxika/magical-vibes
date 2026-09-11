package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OPC2", collectorNumber = "23")
public class KilnspireDistrict extends Card {

    public KilnspireDistrict() {
        SequenceEffect addChargeAndMana = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.CHARGE),
                new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.CHARGE)));
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, addChargeAndMana);
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, addChargeAndMana);
        addEffect(EffectSlot.CHAOS_TRIGGERED, new PayXManaDealXDamageToAnyTargetEffect("{X}"));
    }
}
