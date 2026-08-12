package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

@CardRegistration(set = "ZEN", collectorNumber = "195")
public class AdventuringGear extends Card {

    public AdventuringGear() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2)));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
