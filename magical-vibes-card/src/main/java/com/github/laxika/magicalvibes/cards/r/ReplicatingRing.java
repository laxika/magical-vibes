package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "244")
public class ReplicatingRing extends Card {

    public ReplicatingRing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.NIGHT),
                new ConditionalEffect(
                        new SourceCounterThreshold(8, CounterType.NIGHT),
                        SequenceEffect.of(
                                new RemoveAllCountersEffect(CounterType.NIGHT),
                                CreateTokenEffect.ofSnowArtifactToken(
                                        8,
                                        "Replicated Ring",
                                        List.of(),
                                        List.of(new ActivatedAbility(
                                                true,
                                                null,
                                                List.of(new AwardAnyColorManaEffect()),
                                                "{T}: Add one mana of any color."
                                        ))
                                )
                        )
                )
        ));
    }
}
