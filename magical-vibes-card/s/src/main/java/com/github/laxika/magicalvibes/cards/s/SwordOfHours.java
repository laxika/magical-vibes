package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.RollD12AndResolveIfGreaterThanEventValueEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "61")
public class SwordOfHours extends Card {

    public SwordOfHours() {
        addTrigger(Triggers.attacks(),
                new PutCounterOnTargetPermanentEffect(Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.equippedCreatureDealsCombatDamage(),
                new RollD12AndResolveIfGreaterThanEventValueEffect(
                        new DoublePlusOneCountersOnTargetCreatureEffect(Subjects.triggering())));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
