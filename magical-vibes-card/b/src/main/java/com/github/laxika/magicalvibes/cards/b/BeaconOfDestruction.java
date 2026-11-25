package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "189")
@CardRegistration(set = "5DN", collectorNumber = "61")
@CardRegistration(set = "DDS", collectorNumber = "35")
public class BeaconOfDestruction extends Card {

    public BeaconOfDestruction() {
        addSpellEffect(DealDamageEffect.toAnyTarget(5));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
