package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetAnteCardWithTopOfLibraryEffect;

@CardRegistration(set = "SUM", collectorNumber = "100")
public class Darkpact extends Card {

    public Darkpact() {
        addEffect(EffectSlot.SPELL, new ExchangeTargetAnteCardWithTopOfLibraryEffect());
    }
}
