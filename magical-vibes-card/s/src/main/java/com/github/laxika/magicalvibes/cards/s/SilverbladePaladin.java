package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCanSoulbond;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
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

@CardRegistration(set = "AVR", collectorNumber = "36")
@CardRegistration(set = "C14", collectorNumber = "88")
public class SilverbladePaladin extends Card {

    public SilverbladePaladin() {
        // Soulbond (CR 702.95): may pair when this or another unpaired creature enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCanSoulbond()),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Silverblade Paladin with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new AllConditions(List.of(
                        new NotCondition(new SourceIsPaired()),
                        new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsPairedPredicate()))))))),
                new MayEffect(new SoulbondPairWithEnteringEffect(),
                        "Pair Silverblade Paladin with that creature?"));

        // As long as this creature is paired with another creature, both creatures have double strike.
        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF_AND_PAIRED)));
    }
}
