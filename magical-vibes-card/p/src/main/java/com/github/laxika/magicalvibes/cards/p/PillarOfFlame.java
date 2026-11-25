package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "AVR", collectorNumber = "149")
@CardRegistration(set = "IMA", collectorNumber = "141")
public class PillarOfFlame extends Card {

    public PillarOfFlame() {
        // 2 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(DealDamageEffect.toAnyTarget(new Fixed(2)).exilingDamagedCreaturesThatWouldDie());
    }
}
