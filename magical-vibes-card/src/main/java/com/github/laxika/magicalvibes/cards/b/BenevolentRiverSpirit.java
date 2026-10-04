package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "TLA", collectorNumber = "45")
public class BenevolentRiverSpirit extends Card {

    public BenevolentRiverSpirit() {
        addEffect(EffectSlot.SPELL, new WaterbendCost(5));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
    }
}
