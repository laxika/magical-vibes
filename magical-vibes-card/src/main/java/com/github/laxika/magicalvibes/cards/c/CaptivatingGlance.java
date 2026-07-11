package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashForControlOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "55")
public class CaptivatingGlance extends Card {

    public CaptivatingGlance() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // At the beginning of your end step, clash with an opponent. If you win, gain control
                // of enchanted creature. Otherwise, that player gains control of enchanted creature.
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new ClashForControlOfEnchantedCreatureEffect());
    }
}
