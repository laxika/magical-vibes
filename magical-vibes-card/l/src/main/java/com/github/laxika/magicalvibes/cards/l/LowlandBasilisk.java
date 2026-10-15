package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;

@CardRegistration(set = "STH", collectorNumber = "109")
@CardRegistration(set = "TPR", collectorNumber = "178")
public class LowlandBasilisk extends Card {

    public LowlandBasilisk() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                DestroyDamagedCreatureAtEndOfCombatEffect.thisCreature());
    }
}
