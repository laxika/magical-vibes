package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardOfChosenCreatureTypeIntoLibraryEffect;

@CardRegistration(set = "LGN", collectorNumber = "124")
public class ElvishSoultiller extends Card {

    public ElvishSoultiller() {
        addEffect(EffectSlot.ON_DEATH, new ShuffleGraveyardOfChosenCreatureTypeIntoLibraryEffect());
    }
}
