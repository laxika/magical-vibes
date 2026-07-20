package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedOrBlockedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "37")
public class VizierOfDeferment extends Card {

    public VizierOfDeferment() {
        // Flash (auto-loaded). When Vizier of Deferment enters, you may exile target creature if it
        // attacked or blocked this turn. Return that card to the battlefield under its owner's control
        // at the beginning of the next end step.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAttackedOrBlockedThisTurnPredicate()
                )),
                "Target must be a creature that attacked or blocked this turn"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(FlickerEffect.exileTargetReturnAtEndStep(),
                        "Exile target creature that attacked or blocked this turn?"));
    }
}
