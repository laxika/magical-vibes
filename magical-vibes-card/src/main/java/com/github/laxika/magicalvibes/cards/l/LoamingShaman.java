package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "M20", collectorNumber = "180")
public class LoamingShaman extends Card {

    public LoamingShaman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, Integer.MAX_VALUE));
    }
}
