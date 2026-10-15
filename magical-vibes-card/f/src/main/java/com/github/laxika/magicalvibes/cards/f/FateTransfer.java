package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SHM", collectorNumber = "161")
public class FateTransfer extends Card {

    public FateTransfer() {
        // "Move all counters from target creature onto another target creature."
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "First target must be a creature"))
                .addEffect(EffectSlot.SPELL, new MoveCounterFromTargetCreatureToTargetCreatureEffect(true));
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Second target must be a creature"));
    }
}
