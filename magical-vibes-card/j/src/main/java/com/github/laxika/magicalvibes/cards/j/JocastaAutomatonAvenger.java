package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "108")
@CardRegistration(set = "MSC", collectorNumber = "439")
public class JocastaAutomatonAvenger extends Card {

    public JocastaAutomatonAvenger() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCommanderPredicate(true)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new HasAttacker(new PermanentIsCommanderPredicate(true))),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .enterAttacking(true)
                                .build(),
                        "Return Jocasta, Automaton Avenger from your graveyard to the battlefield tapped and attacking?"));
    }
}
