package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "SOM", collectorNumber = "220")
public class VensersJournal extends Card {

    public VensersJournal() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifePerCardsInHandEffect());
    }
}
