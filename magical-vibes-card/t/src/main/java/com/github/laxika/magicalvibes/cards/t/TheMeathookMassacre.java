package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "122")
@CardRegistration(set = "INR", collectorNumber = "387")
@CardRegistration(set = "MID", collectorNumber = "112")
@CardRegistration(set = "DBL", collectorNumber = "112")
public class TheMeathookMassacre extends Card {

    public TheMeathookMassacre() {
        // When The Meathook Massacre enters, each creature gets -X/-X until end of turn.
        var minusX = new Scaled(new XValue(), -1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllCreaturesEffect(minusX, minusX));

        // Whenever a creature you control dies, each opponent loses 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));

        // Whenever a creature an opponent controls dies, you gain 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), new GainLifeEffect(1));
    }
}
