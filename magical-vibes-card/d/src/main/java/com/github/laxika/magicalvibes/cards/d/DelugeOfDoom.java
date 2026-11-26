package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "DSC", collectorNumber = "18")
@CardRegistration(set = "DSC", collectorNumber = "48")
public class DelugeOfDoom extends Card {

    public DelugeOfDoom() {
        CardTypesAmongCardsInGraveyard cardTypes = new CardTypesAmongCardsInGraveyard();
        addSpellEffect(BoostEffect.allCreatures(
                new Scaled(cardTypes, -1), new Scaled(cardTypes, -1)));
    }
}
