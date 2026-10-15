package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreatureTypesAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "MH1", collectorNumber = "34")
public class ValiantChangeling extends Card {

    public ValiantChangeling() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Min(new Fixed(5), new CreatureTypesAmongControlledCreatures())));
    }
}
