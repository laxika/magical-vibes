package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "AKH", collectorNumber = "81")
public class BonePicker extends Card {

    public BonePicker() {
        // This spell costs {3} less to cast if a creature died this turn. (morbid)
        // Flying and deathtouch are keywords auto-loaded from Scryfall.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Morbid(), new ReduceOwnCastCostEffect(new Fixed(3))));
    }
}
