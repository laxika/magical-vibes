package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CreepingInn;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "264")
public class HostileHostel extends Card {

    public HostileHostel() {
        setBackFaceCard(new CreepingInn());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCountersOnSelfEffect(CounterType.SOUL),
                        new ConditionalEffect(
                                new SourceCounterThreshold(3, CounterType.SOUL),
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(CounterType.SOUL),
                                        new TransformSelfEffect(),
                                        new UntapPermanentsEffect(TapUntapScope.SELF)
                                ))
                ),
                "{1}, {T}, Sacrifice a creature: Put a soul counter on Hostile Hostel. Then if there are three or more soul counters on it, remove those counters, transform it, then untap it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "CreepingInn";
    }
}
