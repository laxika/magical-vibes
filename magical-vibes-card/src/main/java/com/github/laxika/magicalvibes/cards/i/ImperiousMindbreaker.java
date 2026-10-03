package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;

@CardRegistration(set = "VOC", collectorNumber = "33")
@CardRegistration(set = "VOC", collectorNumber = "71")
public class ImperiousMindbreaker extends Card {

    public ImperiousMindbreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Imperious Mindbreaker with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new MillEffect(new SourceToughness(), MillRecipient.EACH_OPPONENT),
                        GrantScope.SELF_AND_PAIRED)));
    }
}
