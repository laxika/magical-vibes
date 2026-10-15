package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

@CardRegistration(set = "EVE", collectorNumber = "171")
public class LeeringEmblem extends Card {

    public LeeringEmblem() {
        // Whenever you cast a spell, equipped creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2)));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
