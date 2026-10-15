package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "175")
public class QuirionBeastcaller extends Card {

    public QuirionBeastcaller() {
        // Whenever you cast a creature spell, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new PutCountersOnSourceEffect(1, 1, 1))));

        // When this creature dies, distribute X +1/+1 counters among any number of target
        // creatures you control, where X is the number of +1/+1 counters on this creature.
        addEffect(EffectSlot.ON_DEATH,
                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
