package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "243")
@CardRegistration(set = "INR", collectorNumber = "433")
@CardRegistration(set = "MID", collectorNumber = "232")
@CardRegistration(set = "DBL", collectorNumber = "232")
public class LiesaForgottenArchangel extends Card {

    public LiesaForgottenArchangel() {
        // Whenever another nontoken creature you control dies, return that card to its owner's hand
        // at the beginning of the next end step. The card is the stack entry's triggering card.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());

        // If a creature an opponent controls would die, exile it instead.
        addStaticEffect(new ExileOpponentCreaturesInsteadOfDyingEffect());
    }
}
