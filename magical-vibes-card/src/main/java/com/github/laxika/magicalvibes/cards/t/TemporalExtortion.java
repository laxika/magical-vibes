package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

@CardRegistration(set = "PLC", collectorNumber = "81")
public class TemporalExtortion extends Card {

    public TemporalExtortion() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new AnyPlayerMayPayLifeToCounterSpellEffect(new HalfControllerLifeRoundedUp()));
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
    }
}
