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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "23")
@CardRegistration(set = "OPCA", collectorNumber = "44")
public class KilnspireDistrict extends Card {

    public KilnspireDistrict() {
        SequenceEffect addChargeAndMana = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.CHARGE),
                new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.CHARGE)));
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, addChargeAndMana);
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), addChargeAndMana);
        addEffect(EffectSlot.CHAOS_TRIGGERED, new PayXManaDealXDamageToAnyTargetEffect("{X}"));
    }
}
