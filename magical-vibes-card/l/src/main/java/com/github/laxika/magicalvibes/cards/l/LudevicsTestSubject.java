package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "64")
public class LudevicsTestSubject extends Card {

    public LudevicsTestSubject() {
        setBackFaceCard(new LudevicsAbomination());

        // Defender is loaded from Scryfall.

        // {1}{U}: Put a hatchling counter on Ludevic's Test Subject.
        // Then if there are five or more hatchling counters on it, remove all of them and transform it.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.HATCHLING),
                        new ConditionalEffect(new SourceCounterThreshold(5, CounterType.HATCHLING), SequenceEffect.of(
                                new RemoveAllCountersEffect(CounterType.HATCHLING),
                                new TransformSelfEffect()))),
                "{1}{U}: Put a hatchling counter on Ludevic's Test Subject. Then if there are five or more hatchling counters on it, remove all of them and transform it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "LudevicsAbomination";
    }
}
