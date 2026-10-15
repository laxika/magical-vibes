package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "28")
public class OriginOfTheAvengers extends Card {

    public OriginOfTheAvengers() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(2));

        CardAllOfPredicate heroCreatureWithManaValueAtMostThree = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.HERO),
                new CardMaxManaValuePredicate(3)));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MayEffect(
                new PutCardToBattlefieldOrElseEffect(heroCreatureWithManaValueAtMostThree, "Hero creature",
                        new DrawCardEffect(1)),
                "Put a Hero creature card with mana value 3 or less from your hand onto the battlefield?",
                new DrawCardEffect(1)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
