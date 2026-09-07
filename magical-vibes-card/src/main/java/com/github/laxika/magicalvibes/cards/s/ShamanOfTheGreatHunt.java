package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "113")
public class ShamanOfTheGreatHunt extends Card {

    public ShamanOfTheGreatHunt() {
        PermanentAllOfPredicate qualifyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G/U}{G/U}",
                List.of(new DrawCardEffect(new PermanentCount(qualifyingCreature, CountScope.CONTROLLER))),
                "{2}{G/U}{G/U}: Draw a card for each creature you control with power 4 or greater."
        ).withActivationCondition(
                new ControlsPermanent(qualifyingCreature),
                "Activate only if you control a creature with power 4 or greater"));
    }
}
