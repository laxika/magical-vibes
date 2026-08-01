package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.AttachedPermanentColorCount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RTR", collectorNumber = "227")
public class CivicSaber extends Card {

    public CivicSaber() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new AttachedPermanentColorCount(), new Fixed(0), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
