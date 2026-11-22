package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "135")
public class Mindmoil extends Card {

    public Mindmoil() {
        addTrigger(Triggers.youCastSpell().anySpell(), PutCardsIntoLibraryEffect.onBottom(Subjects.cardsInYourHand(null)),
                new DrawCardEffect(new AmountThisWay(StepKind.PUT_INTO_LIBRARY), Subjects.you()));
    }
}
