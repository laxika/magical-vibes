package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CHK", collectorNumber = "258")
public class JunkyoBell extends Card {

    public JunkyoBell() {
        // At the beginning of your upkeep, you may have target creature you control get +X/+X
        // until end of turn, where X is the number of creatures you control. If you do, sacrifice
        // that creature at the beginning of the next end step.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)),
                        new SacrificeTargetPermanentAtEndStepEffect()),
                "Have target creature you control get +X/+X until end of turn?"
        ));
    }
}
