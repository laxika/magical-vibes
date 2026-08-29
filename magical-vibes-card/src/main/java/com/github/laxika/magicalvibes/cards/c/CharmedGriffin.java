package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "7")
public class CharmedGriffin extends Card {

    public CharmedGriffin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                EachPlayerMayPutCardFromHandToBattlefieldEffect.eachOpponent(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.ENCHANTMENT))),
                        "artifact or enchantment"));
    }
}
