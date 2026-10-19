package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "260")
@CardRegistration(set = "MMA", collectorNumber = "214")
public class RunedStalactite extends Card {

    public RunedStalactite() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        // "is every creature type" (without granting the changeling ability)
        addEffect(EffectSlot.STATIC, new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
