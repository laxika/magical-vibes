package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "KLD", collectorNumber = "33")
public class TrustyCompanion extends Card {

    public TrustyCompanion() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect(false));
    }
}
