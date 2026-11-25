package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "EMN", collectorNumber = "133")
@CardRegistration(set = "SIR", collectorNumber = "163")
public class IncendiaryFlow extends Card {

    public IncendiaryFlow() {
        // 3 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(DealDamageEffect.toAnyTarget(new Fixed(3)).exilingDamagedCreaturesThatWouldDie());
    }
}
