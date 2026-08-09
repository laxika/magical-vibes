package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnTopOfLibraryEffect;

@CardRegistration(set = "STH", collectorNumber = "66")
public class Mortuary extends Card {

    public Mortuary() {
        // Whenever a creature is put into your graveyard from the battlefield, put that card on
        // top of your library.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutTriggeringCardFromGraveyardOnTopOfLibraryEffect());
    }
}
