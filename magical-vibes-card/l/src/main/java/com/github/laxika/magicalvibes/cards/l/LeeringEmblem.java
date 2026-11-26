package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "171")
public class LeeringEmblem extends Card {

    public LeeringEmblem() {
        // Whenever you cast a spell, equipped creature gets +2/+2 until end of turn.
        addTrigger(Triggers.youCastSpell(),
                BoostEffect.of(Subjects.attached(), new Fixed(2), new Fixed(2)));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
