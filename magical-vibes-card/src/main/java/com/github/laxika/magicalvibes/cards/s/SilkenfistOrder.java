package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "NEM", collectorNumber = "20")
public class SilkenfistOrder extends Card {

    public SilkenfistOrder() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
