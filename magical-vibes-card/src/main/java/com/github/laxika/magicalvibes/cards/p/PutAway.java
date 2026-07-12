package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardFromControllerGraveyardIntoLibraryEffect;

@CardRegistration(set = "SHM", collectorNumber = "48")
public class PutAway extends Card {

    public PutAway() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new ShuffleCardFromControllerGraveyardIntoLibraryEffect(null));
    }
}
