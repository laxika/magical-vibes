package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "113")
public class ShamanOfTheGreatHunt extends Card {

    public ShamanOfTheGreatHunt() {
        PermanentAllOfPredicate qualifyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                AllyCombatDamageTriggerEffect.boundToDealer(new PutCountersOnSourceEffect(1, 1, 1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G/U}{G/U}",
                List.of(new DrawCardEffect(new PermanentCount(qualifyingCreature, CountScope.CONTROLLER))),
                "{2}{G/U}{G/U}: Draw a card for each creature you control with power 4 or greater."
        ));
    }
}
