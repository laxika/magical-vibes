package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "172")
public class CautiousSurvivor extends Card {

    public CautiousSurvivor() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                new GainLifeEffect(2));
    }
}
