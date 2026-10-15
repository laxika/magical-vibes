package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerCantCastSpellsWhileAttackingEffect;

@CardRegistration(set = "FRF", collectorNumber = "30")
public class WardscaleDragon extends Card {

    public WardscaleDragon() {
        addEffect(EffectSlot.STATIC, new DefendingPlayerCantCastSpellsWhileAttackingEffect());
    }
}
