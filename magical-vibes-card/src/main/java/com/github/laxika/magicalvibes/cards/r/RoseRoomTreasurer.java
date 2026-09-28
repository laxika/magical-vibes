package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "51")
@CardRegistration(set = "NCC", collectorNumber = "151")
public class RoseRoomTreasurer extends Card {

    public RoseRoomTreasurer() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                SequenceEffect.of(
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(1),
                                CreateTokenEffect.ofTreasureToken(1)),
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(2),
                                CreateTokenEffect.ofTreasureToken(1)),
                        ConditionalEffect.unless(
                                new NotCondition(new AnyOf(List.of(
                                        new NthAbilityResolutionThisTurn(1),
                                        new NthAbilityResolutionThisTurn(2)))),
                                SequenceEffect.of(
                                        new PayXManaEffect(),
                                        ConditionalEffect.unless(
                                                new EventValueAtLeast(1),
                                                new QueueReflexiveAbilityEffect(
                                                        new DealDamageToAnyTargetEffect(new EventValue()),
                                                        false,
                                                        true))))));
    }
}
