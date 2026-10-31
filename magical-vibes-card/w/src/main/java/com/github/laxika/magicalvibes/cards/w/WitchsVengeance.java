package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

@CardRegistration(set = "ELD", collectorNumber = "111")
@CardRegistration(set = "ELD", collectorNumber = "358")
public class WitchsVengeance extends Card {

    public WitchsVengeance() {
        addSpellEffect(new BoostAllCreaturesOfChosenSubtypeEffect(-3, -3));
    }
}
