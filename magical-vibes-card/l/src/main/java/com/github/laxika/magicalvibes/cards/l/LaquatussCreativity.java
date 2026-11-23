package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ODY", collectorNumber = "88")
public class LaquatussCreativity extends Card {

    public LaquatussCreativity() {
        // "That many" is the number of cards in their hand as they began to draw, however many they drew.
        addSpellEffect(new NoteAmountEffect(new CardsInHand(CountScope.TARGET_PLAYER)));
        addSpellEffect(new DrawCardEffect(AmountThisWay.noted(), Subjects.targetPlayer()));
        addSpellEffect(new DiscardEffect(AmountThisWay.noted(), Subjects.targetPlayer()));
    }
}
