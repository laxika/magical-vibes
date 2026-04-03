package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "99")
public class Reminisce extends Card {

    public Reminisce() {
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect(true));
    }
}
