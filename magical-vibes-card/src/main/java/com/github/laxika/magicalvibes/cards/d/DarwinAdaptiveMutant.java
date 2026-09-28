package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "718")
public class DarwinAdaptiveMutant extends Card {

    public DarwinAdaptiveMutant() {
        // Evolve is keyword-driven; the ally-creature entry scan creates the trigger.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "Remove two +1/+1 counters from Darwin: He gains indestructible until end of turn."
        ));
    }
}
