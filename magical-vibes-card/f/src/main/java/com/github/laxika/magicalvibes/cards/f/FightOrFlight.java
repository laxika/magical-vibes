package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SeparateCreaturesIntoPilesAndChooseAttackersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "16")
public class FightOrFlight extends Card {

    public FightOrFlight() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.OPPONENT),
                new SeparateCreaturesIntoPilesAndChooseAttackersEffect());
    }
}
