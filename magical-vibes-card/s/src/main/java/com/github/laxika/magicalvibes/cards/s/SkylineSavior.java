package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "YSNC", collectorNumber = "3")
public class SkylineSavior extends Card {

    public SkylineSavior() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect(
                        new PermanentTruePredicate(), "permanent"));
    }
}
