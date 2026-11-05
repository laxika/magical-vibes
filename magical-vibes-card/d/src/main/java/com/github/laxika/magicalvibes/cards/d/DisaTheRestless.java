package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M3C", collectorNumber = "1")
@CardRegistration(set = "M3C", collectorNumber = "12")
@CardRegistration(set = "M3C", collectorNumber = "20")
@CardRegistration(set = "M3C", collectorNumber = "28")
@CardRegistration(set = "M3C", collectorNumber = "139")
@CardRegistration(set = "M3C", collectorNumber = "144")
@CardRegistration(set = "M3C", collectorNumber = "148")
public class DisaTheRestless extends Card {

    public DisaTheRestless() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhereButBattlefield(new PermanentAllOfPredicate(List.of(
                        Triggers.PERMANENT_CARD, new PermanentHasSubtypePredicate(CardSubtype.LHURGOYF))),
                        ObjectRelation.ANOTHER_YOU_OWN),
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect());

        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        CreateTokenEffect tarmogoyfToken = new CreateTokenEffect(
                1,
                "Tarmogoyf",
                0,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.LHURGOYF),
                Set.of(),
                Set.of(),
                List.of(
                        StaticAbility.of(new SetPowerToughnessToAmountEffect(
                        cardTypes, new Sum(cardTypes, new Fixed(1))))));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), TriggerOptions.ONE_OR_MORE, tarmogoyfToken);
    }
}
