package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsEventValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "104")
@CardRegistration(set = "CMM", collectorNumber = "168")
public class IsarethTheAwakener extends Card {

    public IsarethTheAwakener() {
        addTrigger(Triggers.attacks(),
                new MayEffect(SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        QueueReflexiveAbilityEffect.withEventValue(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardManaValueEqualsEventValuePredicate())))
                                        .targetGraveyard(true)
                                        .enterWithCounter(CounterType.CORPSE)
                                        .enterWithCounterCount(1)
                                        .exileIfLeavesBattlefield(true)
                                        .build(),
                                AmountThisWay.manaPaid())),
                        "Pay {X} to return a creature card with mana value X from your graveyard?"));
    }
}
