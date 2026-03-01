package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "20")
public class BlueSunsZenith extends Card {

    public BlueSunsZenith() {
        addEffect(EffectSlot.SPELL, new DrawXCardsForTargetPlayerEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
