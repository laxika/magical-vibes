package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessOpponentDealtDamageThisTurnEffect;

@CardRegistration(set = "M11", collectorNumber = "125")
public class BloodcrazedGoblin extends Card {

    public BloodcrazedGoblin() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessOpponentDealtDamageThisTurnEffect());
    }
}
