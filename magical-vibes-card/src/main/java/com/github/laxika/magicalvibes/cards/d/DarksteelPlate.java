package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MBS", collectorNumber = "104")
public class DarksteelPlate extends Card {

    public DarksteelPlate() {
        // Indestructible keyword on the Equipment itself is auto-loaded from Scryfall.
        // Equipped creature has indestructible.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
