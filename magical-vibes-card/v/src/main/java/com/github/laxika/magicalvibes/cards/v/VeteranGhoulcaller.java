package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "34")
public class VeteranGhoulcaller extends Card {

    public VeteranGhoulcaller() {
        addEffect(EffectSlot.ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND,
                new ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect());
    }
}
