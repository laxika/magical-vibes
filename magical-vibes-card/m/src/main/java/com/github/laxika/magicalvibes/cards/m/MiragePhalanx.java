package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;

import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "35")
@CardRegistration(set = "VOC", collectorNumber = "73")
public class MiragePhalanx extends Card {

    public MiragePhalanx() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Mirage Phalanx with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        CreateTokenCopyOfSourceEffect.withoutSourceEffects(
                                Set.of(MayEffect.class, SoulbondPairWithEnteringEffect.class),
                                Set.of(Keyword.SOULBOND), true, true),
                        GrantScope.SELF_AND_PAIRED)));
    }
}
