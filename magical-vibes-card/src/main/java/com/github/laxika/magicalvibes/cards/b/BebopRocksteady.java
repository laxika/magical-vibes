package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "TMT", collectorNumber = "140")
@CardRegistration(set = "TMT", collectorNumber = "204")
@CardRegistration(set = "TMT", collectorNumber = "241")
public class BebopRocksteady extends Card {

    public BebopRocksteady() {
        // Whenever Bebop & Rocksteady attacks or blocks, sacrifice a permanent unless you discard a card.
        addEffect(EffectSlot.ON_ATTACK, sacrificeOrDiscardEffect());
        addEffect(EffectSlot.ON_BLOCK, sacrificeOrDiscardEffect());
    }

    private SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect sacrificeOrDiscardEffect() {
        return new SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
                new PermanentTruePredicate(), 0, 0, 0, "a permanent");
    }
}
