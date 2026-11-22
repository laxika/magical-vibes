package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "118")
public class RuinRaider extends Card {

    public RuinRaider() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // reveal the top card of your library and put that card into your hand.
        // You lose life equal to the card's mana value.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                        new LoseLifeEffect(AmountThisWay.revealedManaValue(), Subjects.you())));
    }
}
