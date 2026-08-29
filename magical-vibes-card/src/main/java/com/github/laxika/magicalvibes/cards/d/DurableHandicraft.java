package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "153")
public class DurableHandicraft extends Card {

    public DurableHandicraft() {
        // Whenever a creature you control enters, you may pay {1}. If you do, put a +1/+1 counter
        // on that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{1}",
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                "Pay {1} to put a +1/+1 counter on that creature?"));

        // {5}{G}, Sacrifice this enchantment: Put a +1/+1 counter on each creature you control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())
                ),
                "{5}{G}, Sacrifice Durable Handicraft: Put a +1/+1 counter on each creature you control."
        ));
    }
}
