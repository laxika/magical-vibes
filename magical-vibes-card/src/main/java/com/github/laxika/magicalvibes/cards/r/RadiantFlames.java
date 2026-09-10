package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "BFZ", collectorNumber = "151")
public class RadiantFlames extends Card {

    public RadiantFlames() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), false));
    }
}
