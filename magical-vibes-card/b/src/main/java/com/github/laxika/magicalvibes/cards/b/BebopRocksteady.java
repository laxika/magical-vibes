package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "140")
@CardRegistration(set = "TMT", collectorNumber = "204")
@CardRegistration(set = "TMT", collectorNumber = "241")
public class BebopRocksteady extends Card {

    public BebopRocksteady() {
        // Whenever Bebop & Rocksteady attacks or blocks, sacrifice a permanent unless you discard a card.
        addTrigger(Triggers.attacks(), sacrificeOrDiscardEffect());
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, sacrificeOrDiscardEffect());
    }

    private SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect sacrificeOrDiscardEffect() {
        return new SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
                new PermanentTruePredicate(), 0, 0, 0, "a permanent");
    }
}
