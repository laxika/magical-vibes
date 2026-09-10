package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "64")
public class SweepAway extends Card {

    public SweepAway() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect(
                        new PermanentIsAttackingPredicate()));
    }
}
