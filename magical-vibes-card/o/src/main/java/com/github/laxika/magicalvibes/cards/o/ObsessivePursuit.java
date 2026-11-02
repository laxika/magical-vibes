package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "112")
public class ObsessivePursuit extends Card {

    public ObsessivePursuit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(new LoseLifeEffect(1), CreateTokenEffect.ofClueToken(1).withInvestigate(false)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(new LoseLifeEffect(1), CreateTokenEffect.ofClueToken(1).withInvestigate(false)));

        PermanentPredicate attackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate()));
        target(new PermanentPredicateTargetFilter(attackingCreature,
                "Target must be an attacking creature"))
                .addTrigger(Triggers.youAttack(), new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentsSacrificedThisTurn(),
                        null,
                        attackingCreature,
                        false,
                        null))
                .addTrigger(Triggers.youAttack(), ConditionalEffect.unless(
                        new ControllerSacrificedPermanentsAtLeastThisTurn(3),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)));
    }
}
