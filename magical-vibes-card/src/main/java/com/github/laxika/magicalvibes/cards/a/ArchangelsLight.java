package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

@CardRegistration(set = "DKA", collectorNumber = "1")
public class ArchangelsLight extends Card {

    public ArchangelsLight() {
        addEffect(EffectSlot.SPELL, new GainLifePerGraveyardCardEffect(2));
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
