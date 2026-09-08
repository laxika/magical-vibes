package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "ONE", collectorNumber = "173")
public class LatticeBladeMantis extends Card {

    public LatticeBladeMantis() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(2)));

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ConditionalEffect.unless(
                        new SourceCounterThreshold(1, CounterType.OIL),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.OIL, 1),
                                new UntapPermanentsEffect(TapUntapScope.SELF),
                                new BoostSelfEffect(1, 1))),
                "Remove an oil counter from Lattice-Blade Mantis?"));
    }
}
