package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "588")
public class CaptainAmericaSkybound extends Card {

    public CaptainAmericaSkybound() {
        PermanentPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                SequenceEffect.of(
                        new PutCounterOnEachMatchingPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, attackingCreature,
                                EachPermanentScope.ALL_PLAYERS),
                        new GrantKeywordEffect(
                                Keyword.INDESTRUCTIBLE, GrantScope.ALL_CREATURES,
                                new PermanentIsAttackingPredicate()))));
    }
}
