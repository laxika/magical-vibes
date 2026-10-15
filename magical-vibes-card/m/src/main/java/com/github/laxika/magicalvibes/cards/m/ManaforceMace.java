package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "CON", collectorNumber = "139")
public class ManaforceMace extends Card {

    public ManaforceMace() {
        // Domain — Equipped creature gets +1/+1 for each basic land type among lands you control.
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new BasicLandTypesAmongControlledLands(),
                new BasicLandTypesAmongControlledLands(),
                GrantScope.EQUIPPED_CREATURE));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
