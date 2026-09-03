package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "22")
public class MysteriousLimousine extends Card {

    public MysteriousLimousine() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicateTargetFilter targetFilter = new PermanentPredicateTargetFilter(
                anotherCreature,
                "Target must be another creature"
        );

        ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect exile =
                new ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect(anotherCreature);
        target(targetFilter, 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exile);
        addEffect(EffectSlot.ON_ATTACK, exile);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
