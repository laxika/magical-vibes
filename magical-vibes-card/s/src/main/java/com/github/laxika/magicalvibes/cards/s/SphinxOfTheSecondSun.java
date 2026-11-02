package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalBeginningPhaseEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1720")
@CardRegistration(set = "MKC", collectorNumber = "118")
public class SphinxOfTheSecondSun extends Card {

    public SphinxOfTheSecondSun() {
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU), new AdditionalBeginningPhaseEffect(true));
    }
}
