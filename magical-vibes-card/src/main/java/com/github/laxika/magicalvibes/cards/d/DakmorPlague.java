package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "P02", collectorNumber = "69")
public class DakmorPlague extends Card {

    public DakmorPlague() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3, true));
    }
}
