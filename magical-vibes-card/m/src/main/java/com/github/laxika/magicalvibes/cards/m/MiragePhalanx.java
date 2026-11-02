package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "35")
@CardRegistration(set = "VOC", collectorNumber = "73")
public class MiragePhalanx extends Card {

    public MiragePhalanx() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Mirage Phalanx with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new SoulbondPairWithEnteringEffect());

        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        CreateTokenCopyOfSourceEffect.withoutSourceEffects(
                                Set.of(MayEffect.class, SoulbondPairWithEnteringEffect.class),
                                Set.of(Keyword.SOULBOND), true, true)),
                        GrantScope.SELF_AND_PAIRED)));
    }
}
