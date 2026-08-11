package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "THS", collectorNumber = "96")
public class MarchOfTheReturned extends Card {

    public MarchOfTheReturned() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2));
    }
}
