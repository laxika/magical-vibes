package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentsAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.PermanentsSacrificedThisTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "112")
public class ObsessivePursuit extends Card {

    public ObsessivePursuit() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(new LoseLifeEffect(1), CreateTokenEffect.ofClueToken(1)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                SequenceEffect.of(new LoseLifeEffect(1), CreateTokenEffect.ofClueToken(1)));

        PermanentPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));
        target(new PermanentPredicateTargetFilter(attackingCreature,
                "Target must be an attacking creature"))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentsSacrificedThisTurn(),
                        null,
                        attackingCreature,
                        false,
                        null))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, ConditionalEffect.unless(
                        new ControllerSacrificedPermanentsAtLeastThisTurn(3),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)));
    }
}
