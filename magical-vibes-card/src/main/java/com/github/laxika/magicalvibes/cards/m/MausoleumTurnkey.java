package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RAV", collectorNumber = "94")
public class MausoleumTurnkey extends Card {

    public MausoleumTurnkey() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
