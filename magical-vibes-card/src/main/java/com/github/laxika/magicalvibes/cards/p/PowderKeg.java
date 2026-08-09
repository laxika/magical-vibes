package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "136")
public class PowderKeg extends Card {

    public PowderKeg() {
        // At the beginning of your upkeep, you may put a fuse counter on this artifact.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.FUSE),
                        "Put a fuse counter on Powder Keg?"));

        // {T}, Sacrifice this artifact: Destroy each artifact and creature with mana value equal to
        // the number of fuse counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                new PermanentManaValueEqualsSourceCountersPredicate(CounterType.FUSE)
                        )))
                ),
                "{T}, Sacrifice Powder Keg: Destroy each artifact and creature with mana value equal to the number of fuse counters on Powder Keg."
        ));
    }
}
