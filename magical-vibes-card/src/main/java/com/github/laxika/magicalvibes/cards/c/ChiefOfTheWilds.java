package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "103")
public class ChiefOfTheWilds extends Card {

    public ChiefOfTheWilds() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.WOLF),
                        new PutCountersOnSourceEffect(1, 1, 2)));

        addEffect(EffectSlot.STATIC, new AdditionalTriggeredAbilityEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.WOLF),
                        new PermanentIsBattlePredicate()))));
    }
}
