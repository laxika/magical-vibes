package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "119")
public class ExperimentOne extends Card {

    public ExperimentOne() {
        // Evolve is keyword-driven; the ally-creature entry scan creates the trigger.
        addActivatedAbility(new ActivatedAbility(
                false,
                "",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.PLUS_ONE_PLUS_ONE),
                        new RegenerateEffect()
                ),
                "Remove two +1/+1 counters from Experiment One: Regenerate Experiment One."
        ));
    }
}
