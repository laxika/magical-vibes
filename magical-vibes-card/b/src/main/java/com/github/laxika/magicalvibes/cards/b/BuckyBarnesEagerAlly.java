package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "586")
public class BuckyBarnesEagerAlly extends Card {

    public BuckyBarnesEagerAlly() {
        CardAnyOfPredicate equipmentHeroOrSoldier = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                new CardSubtypePredicate(CardSubtype.HERO),
                new CardSubtypePredicate(CardSubtype.SOLDIER)));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, equipmentHeroOrSoldier));
    }
}
