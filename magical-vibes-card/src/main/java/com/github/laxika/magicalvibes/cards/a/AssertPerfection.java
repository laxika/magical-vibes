package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "164")
public class AssertPerfection extends Card {

    public AssertPerfection() {
        setNeedsTarget(true);
        setMinTargets(1);
        setMaxTargets(2);
        setMultiTargetFilters(List.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "First target must be a creature you control"
                ),
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                        )),
                        "Second target must be a creature an opponent controls"
                )
        ));
        addEffect(EffectSlot.SPELL, new BoostFirstTargetCreatureEffect(1, 0));
        addEffect(EffectSlot.SPELL, new FirstTargetDealsPowerDamageToSecondTargetEffect());
    }
}
