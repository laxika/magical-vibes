package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "59")
public class QuestForAncientSecrets extends Card {

    public QuestForAncientSecrets() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for Ancient Secrets?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new ShuffleGraveyardIntoLibraryEffect(true)
                ),
                "Remove five quest counters from Quest for Ancient Secrets and sacrifice it: Target player shuffles their graveyard into their library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
