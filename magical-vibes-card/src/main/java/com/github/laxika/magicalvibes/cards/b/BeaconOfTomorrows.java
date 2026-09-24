package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

@CardRegistration(set = "5DN", collectorNumber = "24")
@CardRegistration(set = "DDS", collectorNumber = "2")
@CardRegistration(set = "SLD", collectorNumber = "1497")
public class BeaconOfTomorrows extends Card {

    public BeaconOfTomorrows() {
        target(1, 1)
                .addEffect(EffectSlot.SPELL, new ExtraTurnEffect(1));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
