package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "13")
@CardRegistration(set = "PIP", collectorNumber = "365")
@CardRegistration(set = "PIP", collectorNumber = "541")
@CardRegistration(set = "PIP", collectorNumber = "893")
public class BrotherhoodScribe extends Card {

    public BrotherhoodScribe() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EnergyCountersEffect(1)),
                "Metalcraft — {T}: You get {E}. Activate only if you control three or more artifacts.",
                ActivationTimingRestriction.METALCRAFT
        ));

        addTrigger(Triggers.youGetEnergy(),
                new ConditionalEffect(new ControllerTurn(), new BoostAllOwnCreaturesEffect(1, 1)));
    }
}
