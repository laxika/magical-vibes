package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "EMN", collectorNumber = "62")
public class GeistOfTheArchives extends Card {

    public GeistOfTheArchives() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(1));
    }
}
