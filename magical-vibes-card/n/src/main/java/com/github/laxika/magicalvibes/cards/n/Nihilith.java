package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "72")
public class Nihilith extends Card {

    public Nihilith() {
        // Only while suspended: the ability functions from exile.
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS),
                AbilityZones.EXILE), TriggerOptions.DEFAULT,
                new ConditionalEffect(
                        new SourceCardSuspended(),
                        new MayEffect(new RemoveTimeCounterFromExiledCardEffect(getId()),
                                "Remove a time counter from Nihilith?")));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 7-{1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(7));
    }
}
