package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CantGetCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "39")
public class Suncleanser extends Card {

    public Suncleanser() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove all counters from target creature. It can't have counters put on it for as long as this creature remains on the battlefield.",
                        List.of(RemoveAllCountersFromTargetPermanentEffect.fromTargetCreature(),
                                CantGetCountersEffect.onPermanents(Subjects.targetDeclaredElsewhere(),
                                        Duration.WHILE_SOURCE_REMAINS)),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature"
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent loses all counters. That player can't get counters for as long as this creature remains on the battlefield.",
                        List.of(RemoveAllCountersFromTargetPermanentEffect.fromPlayers(Subjects.targetPlayer()),
                                CantGetCountersEffect.onPlayers(Subjects.targetPlayerDeclaredElsewhere(),
                                        Duration.WHILE_SOURCE_REMAINS)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"
                        )
                )
        ))));
    }
}
