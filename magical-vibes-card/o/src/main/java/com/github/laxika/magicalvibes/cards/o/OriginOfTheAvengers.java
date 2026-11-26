package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "28")
public class OriginOfTheAvengers extends Card {

    public OriginOfTheAvengers() {
        addTrigger(Triggers.chapter(1), new ScryEffect(2));

        CardAllOfPredicate heroCreatureWithManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.HERO),
                new CardMaxManaValuePredicate(3)));
        addTrigger(Triggers.chapter(2), new MayEffect(
                SequenceEffect.of(
                        ChooseCardEffect.upTo(Subjects.cardsInYourHand(heroCreatureWithManaValueAtMostThree), 1,
                                "a Hero creature card with mana value 3 or less from your hand to put onto the battlefield"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(new NotCondition(HappenedThisWay.returned()), new DrawCardEffect(1))),
                "Put a Hero creature card with mana value 3 or less from your hand onto the battlefield?",
                new DrawCardEffect(1)));

        addTrigger(Triggers.chapter(3), PutCountersEffect.onEachYouControl(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
