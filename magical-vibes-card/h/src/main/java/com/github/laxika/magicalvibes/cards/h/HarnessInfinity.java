package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeHandAndGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "STX", collectorNumber = "191")
public class HarnessInfinity extends Card {

    public HarnessInfinity() {
        addEffect(EffectSlot.SPELL, new ExchangeHandAndGraveyardEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
