package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2X2", collectorNumber = "172")
@CardRegistration(set = "C15", collectorNumber = "42")
public class ArjunTheShiftingFlame extends Card {

    public ArjunTheShiftingFlame() {
        addTrigger(Triggers.youCastSpell().anySpell(), PutCardsIntoLibraryEffect.onBottom(Subjects.cardsInYourHand(null)),
                new DrawCardEffect(new AmountThisWay(StepKind.PUT_INTO_LIBRARY), Subjects.you()));
    }
}
