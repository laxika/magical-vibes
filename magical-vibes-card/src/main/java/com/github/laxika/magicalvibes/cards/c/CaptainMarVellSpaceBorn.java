package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "MSH", collectorNumber = "12")
public class CaptainMarVellSpaceBorn extends Card {

    public CaptainMarVellSpaceBorn() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentCastSpellThisTurn(new CardTruePredicate()),
                new GrantFlashToCardTypeEffect(null)));
    }
}
