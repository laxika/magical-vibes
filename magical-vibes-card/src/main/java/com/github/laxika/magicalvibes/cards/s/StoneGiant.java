package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessLessThanSourcePowerPredicate;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "159")
public class StoneGiant extends Card {

    public StoneGiant() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        new DestroyTargetPermanentAtEndStepEffect()
                ),
                "{T}: Target creature you control with toughness less than Stone Giant's power gains flying until end of turn. Destroy that creature at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentToughnessLessThanSourcePowerPredicate()
                        )),
                        "Target must be a creature you control with toughness less than Stone Giant's power"
                )
        ));
    }
}
