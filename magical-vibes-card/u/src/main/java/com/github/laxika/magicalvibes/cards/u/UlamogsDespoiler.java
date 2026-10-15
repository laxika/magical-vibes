package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTwoOpponentOwnedExiledCardsIntoGraveyardOnEnterWithCountersEffect;

@CardRegistration(set = "BFZ", collectorNumber = "16")
public class UlamogsDespoiler extends Card {

    public UlamogsDespoiler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutTwoOpponentOwnedExiledCardsIntoGraveyardOnEnterWithCountersEffect(4));
    }
}
