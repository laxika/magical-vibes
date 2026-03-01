package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutXMinusOneMinusOneCountersOnEachCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "39")
public class BlackSunsZenith extends Card {

    public BlackSunsZenith() {
        addEffect(EffectSlot.SPELL, new PutXMinusOneMinusOneCountersOnEachCreatureEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
