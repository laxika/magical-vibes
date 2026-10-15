package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RVR", collectorNumber = "193")
@CardRegistration(set = "C15", collectorNumber = "46")
public class KarlovOfTheGhostCouncil extends Card {

    public KarlovOfTheGhostCouncil() {
        // Whenever you gain life, put two +1/+1 counters on Karlov.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));

        // {W}{B}, Remove six +1/+1 counters from Karlov: Exile target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{B}",
                List.of(
                        new RemoveCounterFromSourceCost(6, CounterType.PLUS_ONE_PLUS_ONE),
                        new ExileTargetPermanentEffect()
                ),
                "{W}{B}, Remove six +1/+1 counters from Karlov: Exile target creature.",
                TargetFilters.creature()
        ));
    }
}
