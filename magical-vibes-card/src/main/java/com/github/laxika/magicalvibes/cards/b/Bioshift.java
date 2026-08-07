package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "GTC", collectorNumber = "214")
public class Bioshift extends Card {

    public Bioshift() {
        // "Move any number of +1/+1 counters from target creature onto another target creature with
        // the same controller." Position 0 loses the counters, position 1 gains them; the controller
        // chooses how many as the spell resolves.
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "First target must be a creature"))
                .addEffect(EffectSlot.SPELL,
                        new MoveCounterFromTargetCreatureToTargetCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Second target must be a creature"));
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
