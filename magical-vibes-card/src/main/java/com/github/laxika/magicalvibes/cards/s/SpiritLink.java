package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "45")
@CardRegistration(set = "9ED", collectorNumber = "47")
@CardRegistration(set = "8ED", collectorNumber = "47")
@CardRegistration(set = "7ED", collectorNumber = "47")
@CardRegistration(set = "6ED", collectorNumber = "43")
public class SpiritLink extends Card {

    public SpiritLink() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addEffect(EffectSlot.STATIC, new GainLifeEqualToDamageDealtEffect());
    }
}
