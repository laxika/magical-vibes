package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class PhoenixWardenOfFire extends Card {

    public PhoenixWardenOfFire() {
        addTrigger(Triggers.chapter(1),
                DealDamageEffect.to(Subjects.eachOpponent(), 2));
        addTrigger(Triggers.chapter(2),
                DealDamageEffect.to(Subjects.eachOpponent(), 2));
        addTrigger(Triggers.chapter(3),
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.withinTotalManaValue(
                        new CardTypePredicate(CardType.CREATURE), 6),
                new ExileSelfAndReturnTransformedEffect());
    }
}
