package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ODY", collectorNumber = "261")
public class PipersMelody extends Card {

    public PipersMelody() {
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
