package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "15")
public class AmberPlateAinok extends Card {

    public AmberPlateAinok() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                new EndureEffect(1));
    }
}
