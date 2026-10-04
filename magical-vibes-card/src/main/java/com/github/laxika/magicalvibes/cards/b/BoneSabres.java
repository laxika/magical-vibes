package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "40K", collectorNumber = "88")
public class BoneSabres extends Card {

    public BoneSabres() {
        // Whenever equipped creature attacks, put four +1/+1 counters on it.
        addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
