package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AA2", collectorNumber = "12")
@CardRegistration(set = "M3C", collectorNumber = "59")
@CardRegistration(set = "M3C", collectorNumber = "111")
public class Pyrogoyf extends Card {

    public Pyrogoyf() {
        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                cardTypes, new Sum(cardTypes, new Fixed(1))));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.LHURGOYF),
                        DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower())));
    }
}
