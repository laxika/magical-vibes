package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "198")
public class LightningCoils extends Card {

    public LightningCoils() {
        // Whenever a nontoken creature you control dies, put a charge counter on this artifact.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        // At the beginning of your upkeep, if this artifact has five or more charge counters on it,
        // remove all of them and create that many hasty Elemental tokens that are exiled next end step.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.CHARGE),
                SequenceEffect.of(
                        new RemoveAllCountersEffect(CounterType.CHARGE),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                new EventValue(),
                                "Elemental",
                                3,
                                1,
                                CardColor.RED,
                                null,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                true,
                                false,
                                0,
                                Set.of()))));
    }
}
