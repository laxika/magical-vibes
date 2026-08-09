package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "UDS", collectorNumber = "65")
public class PhyrexianNegator extends Card {

    public PhyrexianNegator() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new SacrificePermanentsEffect(
                new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
