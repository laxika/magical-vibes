package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "139")
public class CurseOfChains extends Card {

    public CurseOfChains() {
        target(TargetFilters.creature())
        .addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new TapPermanentsEffect(TapUntapScope.ENCHANTED));
    }
}
