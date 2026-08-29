package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "THB", collectorNumber = "157")
public class StormsWrath extends Card {

    public StormsWrath() {
        // Storm's Wrath deals 4 damage to each creature and each planeswalker.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4, false, false, true, null));
    }
}
