package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "40")
public class CryptChampion extends Card {

    public CryptChampion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        1,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3)))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.RED)), new SacrificeSelfEffect()));
    }
}
