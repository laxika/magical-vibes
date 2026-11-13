package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "6")
@CardRegistration(set = "OM1", collectorNumber = "16")
@CardRegistration(set = "MSC", collectorNumber = "771")
public class DailyBugleReporters extends Card {

    public DailyBugleReporters() {
        PermanentPredicateTargetFilter creatureTarget = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");
        target(creatureTarget, 0, 2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each of up to two target creatures",
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        creatureTarget, null, 0, 2, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card with mana value 2 or less from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardMaxManaValuePredicate(2))))
                                .targetGraveyard(true)
                                .build())
        ))));
    }
}
