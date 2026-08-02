package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;

@CardRegistration(set = "M15", collectorNumber = "219")
public class HotSoup extends Card {

    public HotSoup() {
        // Equipped creature can't be blocked.
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        // Whenever equipped creature is dealt damage, destroy it.
        addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, new DestroyEnchantedPermanentEffect());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
