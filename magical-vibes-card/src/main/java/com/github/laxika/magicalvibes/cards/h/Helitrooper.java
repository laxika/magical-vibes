package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostForTargetingSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "22")
@CardRegistration(set = "FIC", collectorNumber = "106")
public class Helitrooper extends Card {

    private static final PermanentPredicate ANOTHER_ATTACKING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsAttackingPredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
    ));

    public Helitrooper() {
        target(new PermanentPredicateTargetFilter(
                ANOTHER_ATTACKING_CREATURE,
                "Target must be another attacking creature"
        )).addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));

        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostForTargetingSourceEffect(2));
    }
}
