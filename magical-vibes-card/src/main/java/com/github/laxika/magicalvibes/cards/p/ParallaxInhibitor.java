package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "134")
public class ParallaxInhibitor extends Card {

    public ParallaxInhibitor() {
        // {1}, {T}, Sacrifice this artifact: Put a fade counter on each permanent with fading you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.FADE,
                                1,
                                new PermanentHasKeywordPredicate(Keyword.FADING)
                        )
                ),
                "{1}, {T}, Sacrifice this artifact: Put a fade counter on each permanent with fading you control."
        ));
    }
}
