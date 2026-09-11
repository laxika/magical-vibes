package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "OPC2", collectorNumber = "11")
public class AstralArena extends Card {

    public AstralArena() {
        addEffect(EffectSlot.STATIC, new MaximumCombatCreaturesEffect(1, 1));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new MassDamageEffect(2));
    }
}
