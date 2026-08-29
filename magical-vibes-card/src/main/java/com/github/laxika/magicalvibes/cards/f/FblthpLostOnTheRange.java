package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlotNonlandCardsFromTopOfLibraryEffect;

@CardRegistration(set = "OTJ", collectorNumber = "48")
public class FblthpLostOnTheRange extends Card {

    public FblthpLostOnTheRange() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new PlotNonlandCardsFromTopOfLibraryEffect());
    }
}
