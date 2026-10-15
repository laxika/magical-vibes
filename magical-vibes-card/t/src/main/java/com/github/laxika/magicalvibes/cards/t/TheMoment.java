package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "180")
@CardRegistration(set = "WHO", collectorNumber = "459")
@CardRegistration(set = "WHO", collectorNumber = "1050")
public class TheMoment extends Card {

    public TheMoment() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.TIME));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new PhaseOutTargetCreatureUntilSourceLeavesEffect()),
                "{2}, {T}: Untap target creature you control. It phases out until The Moment leaves the battlefield.",
                TargetFilters.creatureYouControl()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentManaValueAtMostSourceCountersPredicate(CounterType.TIME)
                        ))),
                        new SacrificeSelfEffect()),
                "{3}, {T}: Destroy each nonland permanent with mana value less than or equal to the number of time counters on The Moment. "
                        + "Then sacrifice The Moment. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
