package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "LTC", collectorNumber = "43")
@CardRegistration(set = "LTC", collectorNumber = "126")
public class PrizePig extends Card {

    public PrizePig() {
        // Whenever you gain life, put that many ribbon counters on this creature. Then if there are
        // three or more ribbon counters on this creature, remove those counters and untap it.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.RIBBON, new EventValue()),
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.RIBBON),
                        SequenceEffect.of(
                                new RemoveAllCountersEffect(CounterType.RIBBON),
                                new UntapPermanentsEffect(TapUntapScope.SELF)))));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
