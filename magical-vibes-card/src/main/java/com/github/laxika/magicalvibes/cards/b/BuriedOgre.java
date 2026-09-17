package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StartInGraveyardEffect;

@CardRegistration(set = "MB1", collectorNumber = "36")
public class BuriedOgre extends Card {

    public BuriedOgre() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new StartInGraveyardEffect(1),
                "Begin the game with Buried Ogre in your graveyard?"));
    }
}
