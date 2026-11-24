package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHasRadCounters;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "112")
@CardRegistration(set = "PIP", collectorNumber = "640")
public class NightkinAmbusher extends Card {

    public NightkinAmbusher() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GiveRadCountersEffect(4, Subjects.targetPlayer()));

        addStaticEffect(CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer(
                new TargetPlayerHasRadCounters()));
    }
}
