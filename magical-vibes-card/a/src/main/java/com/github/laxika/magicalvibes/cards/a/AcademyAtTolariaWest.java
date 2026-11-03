package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "1")
@CardRegistration(set = "OPCA", collectorNumber = "9")
public class AcademyAtTolariaWest extends Card {

    public AcademyAtTolariaWest() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHandEmpty()),
                new DrawCardEffect(7));
        addTrigger(Triggers.chaosEnsues(), new DiscardHandEffect());
    }
}
