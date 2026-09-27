package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;

@CardRegistration(set = "YMID", collectorNumber = "44")
public class ToralfsDisciple extends Card {

    public ToralfsDisciple() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConjureCardNamedIntoLibraryEffect("Lightning Bolt", 4));
    }
}
