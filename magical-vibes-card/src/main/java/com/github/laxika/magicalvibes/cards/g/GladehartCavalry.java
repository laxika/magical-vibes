package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "132")
public class GladehartCavalry extends Card {

    private static final TriggeringPermanentConditionalEffect DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                    new GainLifeEffect(2));

    public GladehartCavalry() {
        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        target(new PermanentPredicateTargetFilter(otherCreature, "Target must be another creature"), 0, 6)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
    }
}
