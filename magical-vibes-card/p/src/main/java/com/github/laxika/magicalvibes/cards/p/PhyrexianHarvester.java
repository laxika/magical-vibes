package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "13")
public class PhyrexianHarvester extends Card {

    public PhyrexianHarvester() {
        addTrigger(Triggers.dealtDamage(),
                SequenceEffect.of(
                        new SeekCardsToHandEffect(new EventValue(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        RegisterDelayedStepTriggerEffect.aboutSought(Triggers.endStep(ActivePlayer.YOU),
                                DiscardSpecificCardEffect.of(Subjects.cardsThisWay(StepKind.SEEK)))));
    }
}
