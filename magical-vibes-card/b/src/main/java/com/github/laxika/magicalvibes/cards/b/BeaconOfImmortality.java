package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "10")
@CardRegistration(set = "5DN", collectorNumber = "6")
@CardRegistration(set = "E02", collectorNumber = "1")
public class BeaconOfImmortality extends Card {

    public BeaconOfImmortality() {
        addSpellEffect(new DoubleTargetPlayerLifeEffect());
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
