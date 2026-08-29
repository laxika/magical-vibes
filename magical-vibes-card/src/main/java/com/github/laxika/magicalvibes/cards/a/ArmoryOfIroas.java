package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

@CardRegistration(set = "JOU", collectorNumber = "158")
public class ArmoryOfIroas extends Card {

    public ArmoryOfIroas() {
        // Whenever equipped creature attacks, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
