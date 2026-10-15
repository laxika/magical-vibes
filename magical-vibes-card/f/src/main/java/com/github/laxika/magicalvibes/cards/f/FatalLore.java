package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FatalLoreEffect;

@CardRegistration(set = "ALL", collectorNumber = "48")
public class FatalLore extends Card {

    public FatalLore() {
        addEffect(EffectSlot.SPELL, new FatalLoreEffect());
    }
}
