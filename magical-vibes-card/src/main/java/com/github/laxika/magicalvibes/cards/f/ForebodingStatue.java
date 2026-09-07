package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "256")
public class ForebodingStatue extends Card {

    public ForebodingStatue() {
        setBackFaceCard(new ForsakenThresher());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        new PutCountersOnSelfEffect(CounterType.OMEN)
                ),
                "{T}: Add one mana of any color. Put an omen counter on this creature."
        ));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.OMEN),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.SELF),
                                new TransformSelfEffect()
                        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ForsakenThresher";
    }
}
