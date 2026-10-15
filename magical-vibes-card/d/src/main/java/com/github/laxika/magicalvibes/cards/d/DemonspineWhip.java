package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "39")
public class DemonspineWhip extends Card {

    public DemonspineWhip() {
        // {X}: Equipped creature gets +X/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new XValue(), new Fixed(0))),
                "{X}: Equipped creature gets +X/+0 until end of turn."));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
