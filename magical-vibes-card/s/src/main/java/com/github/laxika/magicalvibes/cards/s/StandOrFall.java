package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndChooseBlockersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "171")
public class StandOrFall extends Card {

    public StandOrFall() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new SeparateCreaturesIntoPilesAndChooseBlockersEffect());
    }
}
