package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "DST", collectorNumber = "56")
public class CrazedGoblin extends Card {

    public CrazedGoblin() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
