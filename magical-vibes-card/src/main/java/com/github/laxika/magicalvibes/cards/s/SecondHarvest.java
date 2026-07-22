package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachControlledCreatureTokenEffect;

@CardRegistration(set = "INR", collectorNumber = "213")
public class SecondHarvest extends Card {

    public SecondHarvest() {
        // For each token you control, create a token that's a copy of that permanent.
        addEffect(EffectSlot.SPELL, new CreateTokenCopyOfEachControlledCreatureTokenEffect(false));
    }
}
