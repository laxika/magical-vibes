package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureFromOpponentLibraryEffect;

@CardRegistration(set = "YMID", collectorNumber = "14")
public class CloneCrafter extends Card {

    public CloneCrafter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureRandomCreatureFromOpponentLibraryEffect());
    }
}
