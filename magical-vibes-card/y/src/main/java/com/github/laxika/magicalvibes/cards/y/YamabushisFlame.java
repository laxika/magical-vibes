package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "CHK", collectorNumber = "198")
public class YamabushisFlame extends Card {

    public YamabushisFlame() {
        // 3 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(DealDamageEffect.toAnyTarget(new Fixed(3)).exilingDamagedCreaturesThatWouldDie());
    }
}
