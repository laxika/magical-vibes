package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "16")
@CardRegistration(set = "DBL", collectorNumber = "283")
public class GryffwingCavalry extends Card {

    public GryffwingCavalry() {
        PermanentPredicateTargetFilter attackingCreatureWithoutFlying = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                )),
                "Target must be an attacking creature without flying"
        );

        target(attackingCreatureWithoutFlying).addTrigger(Triggers.attacks(),
                new MayPayManaEffect("{1}{W}",
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        "Pay {1}{W} to give target attacking creature flying until end of turn?"));
    }
}
