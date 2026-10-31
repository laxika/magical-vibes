package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ULG", collectorNumber = "83")
public class LastDitchEffort extends Card {

    public LastDitchEffort() {
        addSpellEffect(new SacrificeAnyNumberOfPermanentsEffect(
                new PermanentIsCreaturePredicate()));
        addSpellEffect(new DealDamageToAnyTargetEffect(new EventValue()));
    }
}
