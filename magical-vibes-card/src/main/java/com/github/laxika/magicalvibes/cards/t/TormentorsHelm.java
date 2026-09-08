package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsDamageToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "KHM", collectorNumber = "155")
public class TormentorsHelm extends Card {

    public TormentorsHelm() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new EquippedCreatureDealsDamageToDefendingPlayerEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
