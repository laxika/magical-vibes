package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "17")
public class ThendarTheOverminer extends Card {

    public ThendarTheOverminer() {
        var nonbasicLand = new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(Triggers.LAND, nonbasicLand)),
                        ObjectRelation.ANY),
                new ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect(
                        Wastes::new, Set.of(CardType.LAND)));

        PermanentPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        PermanentPredicate nonbasicLandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
        target(new PermanentPredicateTargetFilter(
                        nonbasicLandPermanent, "Target must be a nonbasic land"), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new ConditionalEffect(
                                new ControlsPermanentCount(2, tappedCreature),
                                new DestroyTargetPermanentEffect()));
    }
}
