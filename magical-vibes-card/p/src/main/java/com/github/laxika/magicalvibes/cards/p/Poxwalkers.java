package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "49")
public class Poxwalkers extends Card {

    public Poxwalkers() {
        // Whenever you cast a spell from anywhere other than your hand, return Poxwalkers from your
        // graveyard to the battlefield tapped.
        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .build());
    }
}
