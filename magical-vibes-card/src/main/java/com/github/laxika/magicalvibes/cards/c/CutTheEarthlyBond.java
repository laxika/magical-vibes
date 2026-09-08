package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOK", collectorNumber = "32")
public class CutTheEarthlyBond extends Card {

    public CutTheEarthlyBond() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantedPredicate(),
                "Target must be an enchanted permanent"
        )).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
