package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "ONE", collectorNumber = "193")
public class VenomousBrutalizer extends Card {

    public VenomousBrutalizer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayPayManaEffect("{1}{G}", new ProliferateEffect(new Fixed(1)), "Pay {1}{G}?"));
    }
}
