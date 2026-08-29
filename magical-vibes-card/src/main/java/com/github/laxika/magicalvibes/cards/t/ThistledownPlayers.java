package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "35")
public class ThistledownPlayers extends Card {

    public ThistledownPlayers() {
        // Whenever this creature attacks, untap target nonland permanent.
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
