package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "149")
public class FrightshroudCourier extends Card {

    public FrightshroudCourier() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(2, 2, Set.of(Keyword.FEAR))),
                "{2}{B}, {T}: Target Zombie creature gets +2/+2 and has fear for as long as this creature remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)
                        )),
                        "Target must be a Zombie creature"
                )
        ));
    }
}
