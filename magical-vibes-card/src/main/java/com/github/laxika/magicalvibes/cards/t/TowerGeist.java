package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;

@CardRegistration(set = "DKA", collectorNumber = "53")
public class TowerGeist extends Card {

    public TowerGeist() {
        // Flying is loaded from Scryfall
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsChooseNToHandRestToGraveyardEffect(2, 1));
    }
}
