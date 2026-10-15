package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "ORI", collectorNumber = "223")
public class BondedConstruct extends Card {

    public BondedConstruct() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect(false));
    }
}
