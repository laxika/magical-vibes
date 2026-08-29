package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "54")
public class MidnightClock extends Card {

    public MidnightClock() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.HOUR)),
                "{2}{U}: Put an hour counter on this artifact."
        ));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new PutCountersOnSelfEffect(CounterType.HOUR));

        addEffect(EffectSlot.ON_SELF_COUNTERS_PUT, new ConditionalEffect(
                new SourceCounterThreshold(12, CounterType.HOUR),
                SequenceEffect.of(
                        new ShuffleControllerHandAndGraveyardIntoLibraryEffect(),
                        new DrawCardEffect(7),
                        new ExileSelfEffect())));
    }
}
