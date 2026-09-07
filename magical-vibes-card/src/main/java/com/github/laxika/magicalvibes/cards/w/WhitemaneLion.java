package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "PLC", collectorNumber = "22")
public class WhitemaneLion extends Card {

    public WhitemaneLion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentIsCreaturePredicate(), "creature"));
    }
}
