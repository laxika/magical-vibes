package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "ONE", collectorNumber = "159")
public class BloatedContaminator extends Card {

    public BloatedContaminator() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(1));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ProliferateEffect());
    }
}
