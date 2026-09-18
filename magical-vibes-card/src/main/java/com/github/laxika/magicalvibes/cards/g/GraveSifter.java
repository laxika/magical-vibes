package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect;

@CardRegistration(set = "C14", collectorNumber = "44")
public class GraveSifter extends Card {

    public GraveSifter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect());
    }
}
