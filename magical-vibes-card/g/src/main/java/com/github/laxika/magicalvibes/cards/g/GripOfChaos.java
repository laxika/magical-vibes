package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "98")
public class GripOfChaos extends Card {

    public GripOfChaos() {
        addTrigger(Triggers.playerChoosesTargets(), new ReselectTargetAtRandomEffect());
    }
}
