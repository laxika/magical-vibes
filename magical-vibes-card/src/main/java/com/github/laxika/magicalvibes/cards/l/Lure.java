package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "276")
@CardRegistration(set = "8ED", collectorNumber = "263")
@CardRegistration(set = "7ED", collectorNumber = "255")
@CardRegistration(set = "6ED", collectorNumber = "240")
public class Lure extends Card {

    public Lure() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
