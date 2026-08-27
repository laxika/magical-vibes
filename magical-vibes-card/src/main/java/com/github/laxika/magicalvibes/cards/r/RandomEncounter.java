package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndTemporarilyReanimateMilledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

@CardRegistration(set = "FIN", collectorNumber = "150")
public class RandomEncounter extends Card {

    public RandomEncounter() {
        addEffect(EffectSlot.SPELL, new ShuffleLibraryEffect(false));
        addEffect(EffectSlot.SPELL, new MillControllerAndTemporarilyReanimateMilledCreaturesEffect(4));
    }
}
