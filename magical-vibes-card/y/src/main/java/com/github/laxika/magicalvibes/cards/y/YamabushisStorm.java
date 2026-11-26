package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "CHK", collectorNumber = "199")
public class YamabushisStorm extends Card {

    public YamabushisStorm() {
        // 1 damage to each creature; anything dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(DealDamageEffect.toEachCreature(1).exilingDamagedCreaturesThatWouldDie());
    }
}
