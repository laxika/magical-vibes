package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PLC", collectorNumber = "5")
public class DustElemental extends Card {

    public DustElemental() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnNControlledPermanentsToHandEffect(3, new PermanentIsCreaturePredicate(), "creature"));
    }
}
