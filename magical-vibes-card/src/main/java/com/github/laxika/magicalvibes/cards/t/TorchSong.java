package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "222")
public class TorchSong extends Card {

    public TorchSong() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Torch Song?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.VERSE))
                ),
                "{2}{R}, Sacrifice Torch Song: It deals X damage to any target, where X is the number of verse counters on it."
        ));
    }
}
