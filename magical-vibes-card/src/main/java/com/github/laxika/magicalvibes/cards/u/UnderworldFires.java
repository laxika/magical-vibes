package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "THB", collectorNumber = "162")
public class UnderworldFires extends Card {

    public UnderworldFires() {
        addEffect(EffectSlot.SPELL,
                new MassDamageEffect(new Fixed(1), false, true, null, false, true));
    }
}
