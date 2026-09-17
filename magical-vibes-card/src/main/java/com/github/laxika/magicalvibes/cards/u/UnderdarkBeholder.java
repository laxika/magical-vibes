package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageByRemovingCountersOrSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "49")
public class UnderdarkBeholder extends Card {

    public UnderdarkBeholder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.EYEBALL, new Fixed(10)));
        addEffect(EffectSlot.STATIC,
                new PreventDamageByRemovingCountersOrSacrificeEffect(CounterType.EYEBALL));
        addEffect(EffectSlot.ON_ATTACK, new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                new CardAllOfPredicate(List.of(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY),
                                new CardTypePredicate(CardType.ENCHANTMENT))),
                        new CardManaValueLessThanSourceCountersPredicate(CounterType.EYEBALL))),
                true));
    }
}
