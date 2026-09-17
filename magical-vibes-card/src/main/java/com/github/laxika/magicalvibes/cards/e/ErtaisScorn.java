package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "DMU", collectorNumber = "48")
public class ErtaisScorn extends Card {

    public ErtaisScorn() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentCastTwoOrMoreSpellsThisTurn(),
                new ReduceOwnCastCostEffect(new Fixed(1))));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
