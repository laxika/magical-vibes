package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "223")
public class NashiSearcherInTheDark extends Card {

    public NashiSearcherInTheDark() {
        CardAnyOfPredicate legendaryOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
                        new EventValue(), legendaryOrEnchantment, new EventValue()));
    }
}
