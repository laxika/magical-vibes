package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RollD12AndResolveIfGreaterThanEventValueEffect;

@CardRegistration(set = "AFC", collectorNumber = "61")
public class SwordOfHours extends Card {

    public SwordOfHours() {
        addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                new RollD12AndResolveIfGreaterThanEventValueEffect(
                        new DoublePlusOneCountersOnEnchantedCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
