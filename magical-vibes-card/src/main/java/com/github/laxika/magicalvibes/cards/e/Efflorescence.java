package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "144")
public class Efflorescence extends Card {

    public Efflorescence() {
        // Put two +1/+1 counters on target creature. Infusion — if you gained life this turn,
        // that creature also gains trample and indestructible until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new GainedLifeThisTurn(),
                        new GrantKeywordEffect(
                                Set.of(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE),
                                GrantScope.TARGET)));
    }
}
