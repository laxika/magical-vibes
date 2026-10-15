package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "168")
public class ArgothianSprite extends Card {

    public ArgothianSprite() {
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));

        addActivatedAbility(new ActivatedAbility(false, "{7}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "{7}: Put two +1/+1 counters on this creature."));
    }
}
