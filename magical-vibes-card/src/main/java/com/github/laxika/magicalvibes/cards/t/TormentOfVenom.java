package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "HOU", collectorNumber = "79")
public class TormentOfVenom extends Card {

    public TormentOfVenom() {
        // Put three -1/-1 counters on target creature. Its controller loses 3 life unless they
        // sacrifice another nonland permanent of their choice or discard a card.
        // The counter effect owns the single creature target; the punisher piggybacks on it (reads
        // the target's controller), so it's listed after the counters but the creature is still on
        // the battlefield at resolution (lethal counters are cleaned up only by the trailing SBA).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3))
                .addEffect(EffectSlot.SPELL,
                        new LoseLifeUnlessSacrificeNonlandOrDiscardEffect(3, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
