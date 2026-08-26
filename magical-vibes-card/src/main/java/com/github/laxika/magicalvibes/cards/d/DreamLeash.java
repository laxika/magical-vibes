package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "45")
public class DreamLeash extends Card {

    public DreamLeash() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
        setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsTappedPredicate(),
                "Target must be a tapped permanent"
        ));
    }
}
