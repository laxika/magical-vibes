package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "59")
public class DeadeyeNavigator extends Card {

    public DeadeyeNavigator() {
        // Soulbond (CR 702.94): may pair when this or another unpaired creature enters.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Deadeye Navigator with another unpaired creature you control?"));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new SoulbondPairWithEnteringEffect());

        // As long as Deadeye Navigator is paired with another creature, each of those creatures has
        // "{1}{U}: Exile this creature, then return it to the battlefield under your control."
        ActivatedAbility flicker = new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(FlickerEffect.flickerSelfUnderYourControl()),
                "{1}{U}: Exile this creature, then return it to the battlefield under your control."
        );
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsPaired(),
                new GrantActivatedAbilityEffect(flicker, GrantScope.SELF_AND_PAIRED)));
    }
}
