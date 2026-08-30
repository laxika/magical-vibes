package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "FUT", collectorNumber = "52")
public class LogicKnot extends Card {

    public LogicKnot() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
    }
}
