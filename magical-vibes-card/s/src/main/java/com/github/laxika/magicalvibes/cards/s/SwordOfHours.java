package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RollD12AndResolveIfGreaterThanEventValueEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "61")
public class SwordOfHours extends Card {

    public SwordOfHours() {
        addTrigger(Triggers.attacks(),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.equippedCreatureDealsCombatDamage(),
                new RollD12AndResolveIfGreaterThanEventValueEffect(
                        new DoublePlusOneCountersOnEnchantedCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
