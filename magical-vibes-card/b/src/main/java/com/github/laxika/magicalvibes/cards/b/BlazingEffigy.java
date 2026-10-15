package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToSourcePermanentBySourceNameThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "LEG", collectorNumber = "134")
public class BlazingEffigy extends Card {

    public BlazingEffigy() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToTargetCreatureEffect(new Sum(
                new Fixed(3), new DamageDealtToSourcePermanentBySourceNameThisTurn("Blazing Effigy"))));
    }
}
