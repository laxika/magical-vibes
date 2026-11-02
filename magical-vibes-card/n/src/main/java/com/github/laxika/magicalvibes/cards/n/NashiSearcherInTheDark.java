package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "223")
public class NashiSearcherInTheDark extends Card {

    public NashiSearcherInTheDark() {
        CardAnyOfPredicate legendaryOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
                        new EventValue(), legendaryOrEnchantment, new Fixed(Integer.MAX_VALUE)));
    }
}
