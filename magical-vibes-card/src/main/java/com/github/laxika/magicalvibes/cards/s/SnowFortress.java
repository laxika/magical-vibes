package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "337")
public class SnowFortress extends Card {

    public SnowFortress() {
        // {1}: This creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, 0)),
                "{1}: This creature gets +1/+0 until end of turn."
        ));

        // {1}: This creature gets +0/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(0, 1)),
                "{1}: This creature gets +0/+1 until end of turn."
        ));

        // {3}: This creature deals 1 damage to target creature without flying that's attacking you.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{3}: This creature deals 1 damage to target creature without flying that's attacking you.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingSourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )),
                        "Target must be a creature without flying that's attacking you"
                )
        ));
    }
}
