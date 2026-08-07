package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "CHK", collectorNumber = "199")
public class YamabushisStorm extends Card {

    public YamabushisStorm() {
        // 1 damage to each creature; anything dealt damage this way that would die this turn is exiled instead.
        addEffect(EffectSlot.SPELL, MassDamageEffect.exilingDamageToEachCreature(1));
    }
}
