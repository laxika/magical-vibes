package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "1")
@CardRegistration(set = "M3C", collectorNumber = "12")
@CardRegistration(set = "M3C", collectorNumber = "20")
@CardRegistration(set = "M3C", collectorNumber = "28")
@CardRegistration(set = "M3C", collectorNumber = "139")
@CardRegistration(set = "M3C", collectorNumber = "144")
@CardRegistration(set = "M3C", collectorNumber = "148")
public class DisaTheRestless extends Card {

    public DisaTheRestless() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardSubtypePredicate(CardSubtype.LHURGOYF))),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect()));

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
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                        cardTypes, new Sum(cardTypes, new Fixed(1)))));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, tarmogoyfToken, false, true));
    }
}
