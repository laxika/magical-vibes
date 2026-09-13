package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ME3", collectorNumber = "45")
public class OldManOfTheSea extends Card {

    public OldManOfTheSea() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostSourcePowerPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(GainControlOfTargetEffect.whileSourceRemainsTappedAndTargetMatches(targetPredicate)),
                "{T}: Gain control of target creature with power less than or equal to this creature's power "
                        + "for as long as this creature remains tapped and that creature's power remains less "
                        + "than or equal to this creature's power.",
                new PermanentPredicateTargetFilter(targetPredicate,
                        "Target must be a creature with power less than or equal to Old Man of the Sea's power")
        ));
    }
}
