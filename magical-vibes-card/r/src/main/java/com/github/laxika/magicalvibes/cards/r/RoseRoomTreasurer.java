package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NCC", collectorNumber = "51")
@CardRegistration(set = "NCC", collectorNumber = "151")
@CardRegistration(set = "BLC", collectorNumber = "201")
public class RoseRoomTreasurer extends Card {

    public RoseRoomTreasurer() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(1),
                                CreateTokenEffect.ofTreasureToken(1)),
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(2),
                                CreateTokenEffect.ofTreasureToken(1)),
                        new ConditionalEffect(
                                new NotCondition(new AnyOf(List.of(
                                        new NthAbilityResolutionThisTurn(1),
                                        new NthAbilityResolutionThisTurn(2)))),
                                new MayEffect(SequenceEffect.of(
                                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                                        QueueReflexiveAbilityEffect.withEventValue(
                                                DealDamageEffect.toAnyTarget(new EventValue()),
                                                AmountThisWay.manaPaid())),
                                        "Pay {X} to have Rose Room Treasurer deal X damage to any target?"))));
    }
}
