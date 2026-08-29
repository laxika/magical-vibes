package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPutsCardFromHandOnTopOfLibraryEffect;

@CardRegistration(set = "RAV", collectorNumber = "103")
public class SadisticAugermage extends Card {

    public SadisticAugermage() {
        addEffect(EffectSlot.ON_DEATH, new EachPlayerPutsCardFromHandOnTopOfLibraryEffect());
    }
}
