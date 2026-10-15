package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "M20", collectorNumber = "28")
@CardRegistration(set = "BNG", collectorNumber = "19")
public class LoyalPegasus extends Card {

    public LoyalPegasus() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect());
    }
}
