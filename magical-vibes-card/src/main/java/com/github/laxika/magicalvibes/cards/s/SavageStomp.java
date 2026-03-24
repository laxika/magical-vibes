package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FirstTargetFightsSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnFirstTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "205")
public class SavageStomp extends Card {

    public SavageStomp() {
        // This spell costs {2} less to cast if it targets a Dinosaur you control.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingControlledSubtypeEffect(
                CardSubtype.DINOSAUR, 2));

        // Put a +1/+1 counter on target creature you control.
        // Then that creature fights target creature you don't control.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature you control"
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Second target must be a creature you don't control"
        )).addEffect(EffectSlot.SPELL, new PutPlusOnePlusOneCounterOnFirstTargetEffect(1))
          .addEffect(EffectSlot.SPELL, new FirstTargetFightsSecondTargetEffect());
    }
}
