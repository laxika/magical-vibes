package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOC", collectorNumber = "33")
@CardRegistration(set = "VOC", collectorNumber = "71")
public class ImperiousMindbreaker extends Card {

    public ImperiousMindbreaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Imperious Mindbreaker with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new SoulbondPairWithEnteringEffect());

        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.attacks(),
                        new MillEffect(new SourceToughness(), MillRecipient.EACH_OPPONENT)),
                        GrantScope.SELF_AND_PAIRED)));
    }
}
