package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "86")
public class YuanTiMalison extends Card {

    public YuanTiMalison() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfAttackingAloneEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new VentureIntoDungeonEffect());
    }
}
