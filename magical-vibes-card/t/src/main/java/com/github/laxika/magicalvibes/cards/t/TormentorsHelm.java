package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsDamageToDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "155")
public class TormentorsHelm extends Card {

    public TormentorsHelm() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.becomesBlocked(),
                new EquippedCreatureDealsDamageToDefendingPlayerEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
