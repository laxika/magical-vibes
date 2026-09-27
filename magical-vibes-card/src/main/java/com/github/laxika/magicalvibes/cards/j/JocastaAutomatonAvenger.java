package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "MSC", collectorNumber = "108")
@CardRegistration(set = "MSC", collectorNumber = "439")
public class JocastaAutomatonAvenger extends Card {

    public JocastaAutomatonAvenger() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCommanderPredicate(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new HasAttacker(new PermanentIsCommanderPredicate()),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .enterTapped(true)
                                        .enterAttacking(true)
                                        .build(),
                                "Return Jocasta, Automaton Avenger from your graveyard to the battlefield tapped and attacking?")));
    }
}
