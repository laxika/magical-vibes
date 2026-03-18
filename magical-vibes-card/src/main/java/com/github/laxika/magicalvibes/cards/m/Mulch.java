package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsTypeToHandRestToGraveyardEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "196")
public class Mulch extends Card {

    public Mulch() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsTypeToHandRestToGraveyardEffect(
                4, Set.of(CardType.LAND)
        ));
    }
}
