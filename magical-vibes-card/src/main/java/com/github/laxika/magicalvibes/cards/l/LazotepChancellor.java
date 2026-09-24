package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "MH2", collectorNumber = "203")
public class LazotepChancellor extends Card {

    public LazotepChancellor() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new MayPayManaEffect("{1}",
                        new AmassGoblinsEffect(2, CardSubtype.ZOMBIE),
                        "Pay {1} to amass Zombies 2?"));
    }
}
