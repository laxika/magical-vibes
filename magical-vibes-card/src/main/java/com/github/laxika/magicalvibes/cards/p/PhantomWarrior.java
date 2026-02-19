package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "96")
public class PhantomWarrior extends Card {

    public PhantomWarrior() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
