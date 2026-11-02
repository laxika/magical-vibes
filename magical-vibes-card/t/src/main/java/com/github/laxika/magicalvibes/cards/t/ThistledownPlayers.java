package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "35")
public class ThistledownPlayers extends Card {

    public ThistledownPlayers() {
        // Whenever this creature attacks, untap target nonland permanent.
        target(TargetFilters.nonlandPermanent())
                .addTrigger(Triggers.attacks(), new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
