package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCanSoulbond;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPairedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "59")
@CardRegistration(set = "AVR", collectorNumber = "47")
@CardRegistration(set = "MM3", collectorNumber = "36")
@CardRegistration(set = "SLD", collectorNumber = "902")
public class DeadeyeNavigator extends Card {

    public DeadeyeNavigator() {
        // Soulbond (CR 702.94): may pair when this or another unpaired creature enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCanSoulbond()),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Deadeye Navigator with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new AllConditions(List.of(
                        new NotCondition(new SourceIsPaired()),
                        new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsPairedPredicate()))))))),
                new MayEffect(new SoulbondPairWithEnteringEffect(),
                        "Pair Deadeye Navigator with that creature?"));

        // As long as Deadeye Navigator is paired with another creature, each of those creatures has
        // "{1}{U}: Exile this creature, then return it to the battlefield under your control."
        ActivatedAbility flicker = new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(FlickerEffect.flickerSelfUnderYourControl()),
                "{1}{U}: Exile this creature, then return it to the battlefield under your control."
        );
        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantActivatedAbilityEffect(flicker, GrantScope.SELF_AND_PAIRED)));
    }
}
