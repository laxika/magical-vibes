package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "586")
public class BuckyBarnesEagerAlly extends Card {

    public BuckyBarnesEagerAlly() {
        CardAnyOfPredicate equipmentHeroOrSoldier = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                new CardSubtypePredicate(CardSubtype.HERO),
                new CardSubtypePredicate(CardSubtype.SOLDIER)));
        addEffect(EffectSlot.ON_DEATH,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, equipmentHeroOrSoldier));
    }
}
