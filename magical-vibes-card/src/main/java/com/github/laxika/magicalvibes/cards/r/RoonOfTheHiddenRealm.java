package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2173")
public class RoonOfTheHiddenRealm extends Card {

    public RoonOfTheHiddenRealm() {
        // {2}, {T}: Exile another target creature. Return that card to the battlefield under its
        // owner's control at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(FlickerEffect.exileTargetReturnAtEndStep()),
                "{2}, {T}: Exile another target creature. Return that card to the battlefield under its owner's control at the beginning of the next end step.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature"
                )
        ));
    }
}
