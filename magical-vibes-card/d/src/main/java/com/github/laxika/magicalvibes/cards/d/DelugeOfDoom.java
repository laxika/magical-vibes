package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "DSC", collectorNumber = "18")
@CardRegistration(set = "DSC", collectorNumber = "48")
public class DelugeOfDoom extends Card {

    public DelugeOfDoom() {
        CardTypesAmongCardsInGraveyard cardTypes = new CardTypesAmongCardsInGraveyard();
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(cardTypes, -1), new Scaled(cardTypes, -1)));
    }
}
