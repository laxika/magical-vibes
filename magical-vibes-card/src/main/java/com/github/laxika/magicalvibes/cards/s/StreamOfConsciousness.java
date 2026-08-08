package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "BOK", collectorNumber = "53")
public class StreamOfConsciousness extends Card {

    public StreamOfConsciousness() {
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 4));
    }
}
