package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToLifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "137")
@CardRegistration(set = "CMM", collectorNumber = "503")
public class ArchfiendOfDespair extends Card {

    public ArchfiendOfDespair() {
        addStaticEffect(new OpponentsCantGainLifeEffect());
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new EachOpponentLosesLifeEqualToLifeLostThisTurn());
    }
}
