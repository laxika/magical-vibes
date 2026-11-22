package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "21")
public class PerilousIteration extends Card {

    public PerilousIteration() {
        addSpellEffect(SequenceEffect.of(
                        new SeekCardsToHandEffect(new Fixed(1), new CardMaxManaValuePredicate(2)),
                        RegisterDelayedStepTriggerEffect.aboutSought(Triggers.endStep(ActivePlayer.YOU),
                                DiscardSpecificCardEffect.of(Subjects.cardsThisWay(StepKind.SEEK))).waitingForALaterTurn()));
        addSpellEffect(SequenceEffect.of(
                        new SeekCardsToHandEffect(new Fixed(1), new CardMinManaValuePredicate(3)),
                        RegisterDelayedStepTriggerEffect.aboutSought(Triggers.endStep(ActivePlayer.YOU),
                                DiscardSpecificCardEffect.of(Subjects.cardsThisWay(StepKind.SEEK))).waitingForALaterTurn()));
    }
}
