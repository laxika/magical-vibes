package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "139")
public class KrovikanElementalist extends Card {

    public KrovikanElementalist() {
        // {2}{R}: Target creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{R}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{2}{R}: Target creature gets +1/+0 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // {U}{U}: Target creature you control gains flying until end of turn.
        // Sacrifice it at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                false, "{U}{U}",
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        new SacrificeTargetPermanentAtEndStepEffect()
                ),
                "{U}{U}: Target creature you control gains flying until end of turn. "
                        + "Sacrifice it at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
