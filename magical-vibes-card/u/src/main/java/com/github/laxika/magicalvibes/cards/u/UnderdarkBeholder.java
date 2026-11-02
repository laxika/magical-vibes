package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageByRemovingCountersOrSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "49")
public class UnderdarkBeholder extends Card {

    public UnderdarkBeholder() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.EYEBALL, new Fixed(10)));
        addStaticEffect(new PreventDamageByRemovingCountersOrSacrificeEffect(CounterType.EYEBALL));
        addTrigger(Triggers.attacks(), new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY),
                                new CardTypePredicate(CardType.ENCHANTMENT))),
                        new CardManaValueLessThanSourceCountersPredicate(CounterType.EYEBALL))),
                true));
    }
}
