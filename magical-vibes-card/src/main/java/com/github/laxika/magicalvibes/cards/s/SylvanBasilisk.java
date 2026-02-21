package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;

@CardRegistration(set = "10E", collectorNumber = "301")
public class SylvanBasilisk extends Card {

    public SylvanBasilisk() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCreatureBlockingThisEffect());
    }
}
