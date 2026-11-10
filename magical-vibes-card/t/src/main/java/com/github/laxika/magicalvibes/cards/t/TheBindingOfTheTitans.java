package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "166")
public class TheBindingOfTheTitans extends Card {

    public TheBindingOfTheTitans() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MillEffect(3, MillRecipient.EACH_OPPONENT)));
        addTrigger(Triggers.chapter(2),
                ExileGraveyardCardsEffect.targetedFromAnyGraveyardWithEventValue(
                        2, new CardTypePredicate(CardType.CREATURE)),
                new GainLifeEffect(new EventValue()));
        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND))))
                .targetGraveyard(true)
                .build());
    }
}
