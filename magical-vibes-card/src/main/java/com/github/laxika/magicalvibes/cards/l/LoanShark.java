package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "55")
public class LoanShark extends Card {

    public LoanShark() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new ControllerCastTwoOrMoreSpellsThisTurn(new CardTruePredicate()),
                        new DrawCardEffect()));
    }
}
