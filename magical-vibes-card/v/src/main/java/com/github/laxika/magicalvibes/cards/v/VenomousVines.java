package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "JUD", collectorNumber = "136")
public class VenomousVines extends Card {

    public VenomousVines() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantedPredicate(),
                "Target must be an enchanted permanent"
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(new PermanentIsEnchantedPredicate()));
    }
}
