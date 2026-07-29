package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "323")
public class VentifactBottle extends Card {

    public VentifactBottle() {
        // {X}{1}, {T}: Put X charge counters on this artifact. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE, new XValue())),
                "{X}{1}, {T}: Put X charge counters on this artifact. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        // At the beginning of your first main phase, if this artifact has a charge counter on it,
        // tap it and remove all charge counters from it. Add {C} for each charge counter removed
        // this way. One SequenceEffect so the three steps share a stack entry: the removal
        // snapshots the removed count as the entry's event value, which the mana award reads back.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new ConditionalEffect(new SourceCounterThreshold(1, CounterType.CHARGE),
                        SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.SELF),
                                new RemoveAllCountersFromSelfEffect(CounterType.CHARGE),
                                new AwardManaEffect(ManaColor.COLORLESS, new EventValue()))));
    }
}
