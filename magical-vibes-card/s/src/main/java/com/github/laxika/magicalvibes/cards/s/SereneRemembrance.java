package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "GTC", collectorNumber = "132")
public class SereneRemembrance extends Card {

    public SereneRemembrance() {
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
