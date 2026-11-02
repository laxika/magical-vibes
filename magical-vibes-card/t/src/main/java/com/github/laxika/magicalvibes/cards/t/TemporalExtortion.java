package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "81")
@CardRegistration(set = "SLD", collectorNumber = "1859")
public class TemporalExtortion extends Card {

    public TemporalExtortion() {
        addTrigger(Triggers.youCastThisSpell(),
                new AnyPlayerMayPayLifeToCounterSpellEffect(new HalfControllerLifeRoundedUp()));
        addSpellEffect(new ControllerExtraTurnEffect(1));
    }
}
