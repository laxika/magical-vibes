package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysEffect;

@CardRegistration(set = "DTK", collectorNumber = "149")
public class QalSismaBehemoth extends Card {

    public QalSismaBehemoth() {
        // This creature can't attack or block unless you pay {2}.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessPaysEffect(2));
    }
}
