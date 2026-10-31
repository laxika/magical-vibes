package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "VOW", collectorNumber = "150")
@CardRegistration(set = "DBL", collectorNumber = "417")
public class ChangeOfFortune extends Card {

    public ChangeOfFortune() {
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new DrawCardEffect(new CardsDiscardedOrCycledThisTurn()));
    }
}
