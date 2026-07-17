package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "ALA", collectorNumber = "206")
public class ViolentUltimatum extends Card {

    public ViolentUltimatum() {
        // Destroy three target permanents. A single fixed-count (exactly 3) target group;
        // every chosen permanent is destroyed via DestroyEachTargetPermanentEffect.
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Targets must be permanents"), 3, 3)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
