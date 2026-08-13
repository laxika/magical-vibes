package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "300")
public class LotusBlossom extends Card {

    public LotusBlossom() {
        // At the beginning of your upkeep, you may put a petal counter on this artifact.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.PETAL),
                "Put a petal counter on Lotus Blossom?"
        ));

        // {T}, Sacrifice this artifact: Add X mana of any one color, where X is the number of
        // petal counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new AwardAnyColorManaEffect(new CountersOnSource(CounterType.PETAL))),
                "{T}, Sacrifice this artifact: Add X mana of any one color, where X is the number of petal counters on this artifact."
        ));
    }
}
