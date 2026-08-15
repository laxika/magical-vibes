package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ULG", collectorNumber = "83")
public class LastDitchEffort extends Card {

    public LastDitchEffort() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsEffect(
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new EventValue()));
    }
}
