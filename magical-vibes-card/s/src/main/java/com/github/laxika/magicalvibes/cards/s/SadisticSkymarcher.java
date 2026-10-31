package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;

@CardRegistration(set = "RIX", collectorNumber = "85")
public class SadisticSkymarcher extends Card {

    public SadisticSkymarcher() {
        // As an additional cost to cast this spell, reveal a Vampire card from your hand or pay {1}.
        // Flying and lifelink are auto-loaded from Scryfall.
        addStaticEffect(new IncreaseOwnCastCostUnlessRevealSubtypeEffect(1, CardSubtype.VAMPIRE));
    }
}
