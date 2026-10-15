package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "133")
public class BondedHorncrest extends Card {

    public BondedHorncrest() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect());
    }
}
