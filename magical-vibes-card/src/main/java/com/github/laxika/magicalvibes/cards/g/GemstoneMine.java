package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "164")
@CardRegistration(set = "TSB", collectorNumber = "119")
public class GemstoneMine extends Card {

    public GemstoneMine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINING, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.MINING),
                        new AwardAnyColorManaEffect(),
                        new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.MINING)),
                                new SacrificeSelfEffect())
                ),
                "{T}, Remove a mining counter from this land: Add one mana of any color."
        ));

    }
}
