package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class ForsakenThresher extends Card {

    public ForsakenThresher() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new AwardAnyColorManaEffect());
    }
}
