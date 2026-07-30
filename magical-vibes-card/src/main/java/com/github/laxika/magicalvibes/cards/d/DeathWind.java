package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "AVR", collectorNumber = "93")
public class DeathWind extends Card {

    public DeathWind() {
        // Target creature gets -X/-X until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Scaled(new XValue(), -1),
                new Scaled(new XValue(), -1)
        ));
    }
}
