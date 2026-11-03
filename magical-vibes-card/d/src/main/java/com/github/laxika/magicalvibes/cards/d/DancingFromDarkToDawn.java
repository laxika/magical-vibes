package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "123")
@CardRegistration(set = "HOC", collectorNumber = "123")
public class DancingFromDarkToDawn extends Card {

    public DancingFromDarkToDawn() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.CREATURE)),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));

        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new CreateTokenEffect(
                "Bear", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.BEAR), Set.of(), Set.of()));
    }
}
