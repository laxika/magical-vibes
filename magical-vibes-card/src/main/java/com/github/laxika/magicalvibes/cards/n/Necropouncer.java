package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "147")
public class Necropouncer extends Card {

    public Necropouncer() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +3/+1 and has haste
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
