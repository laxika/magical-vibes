package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

@CardRegistration(set = "WTH", collectorNumber = "46")
public class ParadigmShift extends Card {

    public ParadigmShift() {
        // Exile all cards from your library. Then shuffle your graveyard into your library.
        addEffect(EffectSlot.SPELL, new ExileControllerLibraryEffect());
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
