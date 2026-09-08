package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "STX", collectorNumber = "244")
public class VanishingVerse extends Card {

    public VanishingVerse() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsMonocoloredPredicate(),
                "Target must be a monocolored permanent"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
