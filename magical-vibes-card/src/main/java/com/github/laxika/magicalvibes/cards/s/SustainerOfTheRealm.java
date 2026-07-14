package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "52")
public class SustainerOfTheRealm extends Card {

    public SustainerOfTheRealm() {
        // Flying comes from Scryfall keywords.
        // Whenever this creature blocks, it gets +0/+2 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(0, 2));
    }
}
