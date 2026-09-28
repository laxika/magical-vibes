package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "51")
public class FirefluxSquad extends Card {

    public FirefluxSquad() {
        var anotherAttackingCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                anotherAttackingCreatureYouControl,
                "Target must be another attacking creature you control"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetAttackingCreatureThenRevealUntilCreatureToBattlefieldEffect(
                                anotherAttackingCreatureYouControl));
    }
}
