package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceThenDealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "111")
@CardRegistration(set = "LEG", collectorNumber = "298")
public class VoodooDoll extends Card {

    public VoodooDoll() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.PIN));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new SourceUntapped(),
                new DestroySourceThenDealDamageToControllerEffect(new CountersOnSource(CounterType.PIN))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.PIN))),
                "{X}{X}, {T}: This artifact deals damage equal to the number of pin counters on it to any target."
        ));
    }
}
