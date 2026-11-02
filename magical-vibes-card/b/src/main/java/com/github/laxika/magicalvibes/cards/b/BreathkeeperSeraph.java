package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1345")
@CardRegistration(set = "VOC", collectorNumber = "31")
@CardRegistration(set = "VOC", collectorNumber = "69")
public class BreathkeeperSeraph extends Card {

    public BreathkeeperSeraph() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SoulbondChoosePartnerEffect(),
                        "Pair Breathkeeper Seraph with another unpaired creature you control?"));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new SoulbondPairWithEnteringEffect());

        addStaticEffect(new ConditionalEffect(
                new SourceIsPaired(),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                        new MayEffect(
                                RegisterDelayedSelfReturnFromGraveyardEffect.returningAtControllersNextUpkeep(),
                                "Return this creature to the battlefield at the beginning of your next upkeep?")),
                        GrantScope.SELF_AND_PAIRED)));
    }
}
