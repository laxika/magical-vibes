package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "126")
@CardRegistration(set = "DMR", collectorNumber = "155")
@CardRegistration(set = "C13", collectorNumber = "141")
@CardRegistration(set = "CMD", collectorNumber = "150")
public class DeadwoodTreefolk extends Card {

    public DeadwoodTreefolk() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));

        ReturnCardFromGraveyardEffect returnAnotherCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardIsSelfPredicate()))))
                .targetGraveyard(true)
                .build();
        addTrigger(Triggers.enters(ObjectRelation.THIS), returnAnotherCreature);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), returnAnotherCreature);
    }
}
