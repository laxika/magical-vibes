package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "10E", collectorNumber = "276")
public class Lure extends Card {

    public Lure() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
