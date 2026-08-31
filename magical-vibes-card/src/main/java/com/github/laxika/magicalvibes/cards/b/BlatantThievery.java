package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ONS", collectorNumber = "71")
public class BlatantThievery extends Card {

    public BlatantThievery() {
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Target must be a permanent an opponent controls"
        ), 0, 99).addEffect(EffectSlot.SPELL,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT));
    }
}
