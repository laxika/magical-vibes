package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "POR", collectorNumber = "130")
public class ForkedLightning extends Card {

    public ForkedLightning() {
        // Forked Lightning deals 4 damage divided as you choose among
        // one, two, or three target creatures.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ), 1, 3).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(4));
    }
}
