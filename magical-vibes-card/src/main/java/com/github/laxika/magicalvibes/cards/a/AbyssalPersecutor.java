package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantWinGameEffect;

@CardRegistration(set = "WWK", collectorNumber = "47")
public class AbyssalPersecutor extends Card {

    public AbyssalPersecutor() {
        addEffect(EffectSlot.STATIC, new CantWinGameEffect());
    }
}
