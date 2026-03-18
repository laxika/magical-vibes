package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "NPH", collectorNumber = "60")
public class EvilPresence extends Card {

    public EvilPresence() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
    }
}
