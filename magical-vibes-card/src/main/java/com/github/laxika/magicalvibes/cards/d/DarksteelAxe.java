package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOM", collectorNumber = "149")
public class DarksteelAxe extends Card {

    public DarksteelAxe() {
        // Indestructible keyword is auto-loaded from Scryfall.
        // Equipped creature gets +2/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
