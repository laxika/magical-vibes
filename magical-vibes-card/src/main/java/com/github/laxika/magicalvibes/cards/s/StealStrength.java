package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "PCY", collectorNumber = "79")
public class StealStrength extends Card {

    public StealStrength() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be another creature"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
    }
}
