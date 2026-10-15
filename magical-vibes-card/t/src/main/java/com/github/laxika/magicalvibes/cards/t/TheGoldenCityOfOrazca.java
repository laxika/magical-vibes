package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MOC", collectorNumber = "52")
public class TheGoldenCityOfOrazca extends Card {

    public TheGoldenCityOfOrazca() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        SequenceEffect.of(
                                CreateTokenEffect.ofTreasureToken(1),
                                new ConditionalEffect(new ControllerHasCityBlessing(), new DrawCardEffect(1))),
                        false,
                        true));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(
                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent", true),
                "Put a permanent card from your hand onto the battlefield tapped?"));
    }
}
