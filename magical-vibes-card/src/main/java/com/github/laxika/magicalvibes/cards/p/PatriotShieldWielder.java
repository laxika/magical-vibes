package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "30")
public class PatriotShieldWielder extends Card {

    public PatriotShieldWielder() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)
                ),
                "{2}, {T}: Another target creature you control gets +2/+0 and gains hexproof until end of turn. "
                        + "(It can't be the target of spells or abilities your opponents control.)",
                new ControlledPermanentPredicateTargetFilter(
                        targetFilter,
                        "Target must be another creature you control"
                )
        ));
    }
}
