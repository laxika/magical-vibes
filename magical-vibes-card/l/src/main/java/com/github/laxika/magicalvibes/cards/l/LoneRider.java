package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.ItThatRidesAsOne;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "33")
@CardRegistration(set = "SIR", collectorNumber = "36")
public class LoneRider extends Card {

    public LoneRider() {
        setBackFaceCard(new ItThatRidesAsOne());

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn(3)),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ItThatRidesAsOne";
    }
}
