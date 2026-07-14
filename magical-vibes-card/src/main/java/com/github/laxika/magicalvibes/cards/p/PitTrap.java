package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "313")
public class PitTrap extends Card {

    public PitTrap() {
        // {2}, {T}, Sacrifice this artifact: Destroy target attacking creature without flying. It can't be regenerated.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(true)),
                "{2}, {T}, Sacrifice Pit Trap: Destroy target attacking creature without flying. It can't be regenerated.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )),
                        "Target must be an attacking creature without flying"
                )
        ));
    }
}
