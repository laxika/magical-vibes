package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "KHM", collectorNumber = "127")
public class CravenHulk extends Card {

    public CravenHulk() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect(true, false));
    }
}
