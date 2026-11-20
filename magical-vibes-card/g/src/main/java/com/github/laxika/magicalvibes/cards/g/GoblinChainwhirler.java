package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "129")
public class GoblinChainwhirler extends Card {

    public GoblinChainwhirler() {
        // When Goblin Chainwhirler enters the battlefield, it deals 1 damage to each opponent
        // and each creature and planeswalker they control.
        addSimultaneousTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(1, Subjects.eachOpponent()),
                new DealDamageToPermanentsTargetControlsEffect(1, Subjects.controlledBy(Subjects.eachOpponent(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate())))));
    }
}
