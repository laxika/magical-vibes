package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndTemporarilyReanimateMilledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

@CardRegistration(set = "FIN", collectorNumber = "150")
public class RandomEncounter extends Card {

    public RandomEncounter() {
        addSpellEffect(new ShuffleLibraryEffect(false));
        addSpellEffect(new MillControllerAndTemporarilyReanimateMilledCreaturesEffect(4));
        addCastingOption(new FlashbackCast("{6}{R}{R}"));
    }
}
