package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "75")
public class TigerSeal extends Card {

    public TigerSeal() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new TapPermanentsEffect(TapUntapScope.SELF));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
