package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "21")
public class AriusFlybyTrawler extends Card {

    public AriusFlybyTrawler() {
        addTrigger(Triggers.attacks(),
                SequenceEffect.of(
                        new SeekCardsToHandEffect(new Fixed(1), new CardNotPredicate(new CardSubtypePredicate(CardSubtype.SHARK))),
                        RegisterDelayedStepTriggerEffect.aboutSought(Triggers.endStep(ActivePlayer.ANY),
                                DiscardSpecificCardEffect.of(Subjects.cardsThisWay(StepKind.SEEK)))));
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
