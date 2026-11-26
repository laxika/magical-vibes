package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOU", collectorNumber = "79")
public class TormentOfVenom extends Card {

    public TormentOfVenom() {
        // Put three -1/-1 counters on target creature. Its controller loses 3 life unless they
        // sacrifice another nonland permanent of their choice or discard a card.
        // The counter effect owns the single creature target; the punisher piggybacks on it (reads
        // the target's controller), so it's listed after the counters but the creature is still on
        // the battlefield at resolution (lethal counters are cleaned up only by the trailing SBA).
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, 3))
                .addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()),
                        AnyOfCost.of(new SacrificePermanentCost(new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))),
                                "nonland permanent other than that creature", false), new DiscardCardTypeCost(null, null)),
                        new LoseLifeEffect(3, Subjects.controllerOf(Subjects.target()))));
    }
}
