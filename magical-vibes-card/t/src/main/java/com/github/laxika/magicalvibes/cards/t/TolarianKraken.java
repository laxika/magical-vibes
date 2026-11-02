package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayManaThenMayTapOrUntapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "80")
public class TolarianKraken extends Card {

    public TolarianKraken() {
        addTrigger(Triggers.youDrawCard(),
                new PayManaThenMayTapOrUntapTargetCreatureEffect("{1}"));
    }
}
