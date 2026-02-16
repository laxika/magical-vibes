package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

public class PhantomWarrior extends Card {

    public PhantomWarrior() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
