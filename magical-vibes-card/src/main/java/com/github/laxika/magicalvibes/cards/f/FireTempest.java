package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "POR", collectorNumber = "128")
public class FireTempest extends Card {

    public FireTempest() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(6, true));
    }
}
