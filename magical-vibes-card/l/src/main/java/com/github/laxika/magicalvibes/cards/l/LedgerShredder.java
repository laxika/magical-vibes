package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "46")
@CardRegistration(set = "SPG", collectorNumber = "55")
public class LedgerShredder extends Card {

    public LedgerShredder() {
        addTrigger(Triggers.playerCastsSpell().nthThisTurn(2), new DrawDiscardAndConniveEffect());
    }
}
