package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DKA", collectorNumber = "76")
public class TragicSlip extends Card {

    public TragicSlip() {
        // Target creature gets -1/-1 until end of turn.
        // Morbid — That creature gets -13/-13 until end of turn instead if a creature died this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Morbid(), 
                new BoostTargetCreatureEffect(-1, -1),
                new BoostTargetCreatureEffect(-13, -13)
        ));
    }
}
