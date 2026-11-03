package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UseToughnessForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "17")
public class InterfaceAce extends Card {

    public InterfaceAce() {
        addStaticEffect(new UseToughnessForCrewAndSaddleEffect());
        addTrigger(Triggers.becomesTapped(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
