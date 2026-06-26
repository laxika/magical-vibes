package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DKA", collectorNumber = "46")
public class SavingGrasp extends Card {

    public SavingGrasp() {
        // Return target creature you own to your hand.
        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you own"
        )).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());

        addCastingOption(new FlashbackCast("{W}"));
    }
}
