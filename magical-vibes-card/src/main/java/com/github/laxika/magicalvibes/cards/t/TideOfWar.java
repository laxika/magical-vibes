package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "CHK", collectorNumber = "194")
public class TideOfWar extends Card {

    public TideOfWar() {
        // Whenever one or more creatures block, flip a coin. If you win the flip, each blocking
        // creature is sacrificed by its controller. If you lose the flip, each blocked creature is
        // sacrificed by its controller.
        addEffect(EffectSlot.ON_ANY_CREATURES_BLOCK, new FlipCoinWinEffect(
                new SacrificeEachMatchingPermanentEffect(new PermanentIsBlockingPredicate()),
                new SacrificeEachMatchingPermanentEffect(new PermanentIsBlockedPredicate())));
    }
}
