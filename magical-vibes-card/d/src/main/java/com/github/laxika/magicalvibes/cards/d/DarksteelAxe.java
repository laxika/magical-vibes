package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOM", collectorNumber = "149")
@CardRegistration(set = "MM2", collectorNumber = "208")
@CardRegistration(set = "IMA", collectorNumber = "214")
@CardRegistration(set = "2XM", collectorNumber = "247")
public class DarksteelAxe extends Card {

    public DarksteelAxe() {
        // Indestructible keyword is auto-loaded from Scryfall.
        // Equipped creature gets +2/+0.
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
