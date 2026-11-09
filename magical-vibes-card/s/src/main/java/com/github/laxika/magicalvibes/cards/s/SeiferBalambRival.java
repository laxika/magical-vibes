package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.condition.BlockedByMinCreatures;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "451")
public class SeiferBalambRival extends Card {

    public SeiferBalambRival() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"))
                .addTrigger(Triggers.youAttackPlayer(),
                        new GoadTargetCreatureUntilNextTurnEffect());

        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(
                        new AttackedTargetIsOpponent(), new BlockedByMinCreatures(2)))),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TRIGGERING_PERMANENT));
    }
}
