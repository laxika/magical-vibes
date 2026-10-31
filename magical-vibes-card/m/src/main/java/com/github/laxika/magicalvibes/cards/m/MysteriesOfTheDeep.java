package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "33")
public class MysteriesOfTheDeep extends Card {

    public MysteriesOfTheDeep() {
        addSpellEffect(new ConditionalReplacementEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 1),
                new DrawCardEffect(2),
                new DrawCardEffect(3)
        ));
    }
}
