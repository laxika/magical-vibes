package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostByManaCostEffect;

@CardRegistration(set = "M21", collectorNumber = "48")
public class Discontinuity extends Card {

    public Discontinuity() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new ReduceOwnCastCostByManaCostEffect("{2}{U}{U}")));
        addEffect(EffectSlot.SPELL, new EndTurnEffect());
    }
}
