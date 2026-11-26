package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "82")
public class AlphaBrawl extends Card {

    public AlphaBrawl() {
        // Target creature an opponent controls deals damage equal to its power to each other
        // creature that player controls, then each of those creatures deals damage equal to
        // its power to that creature.
        PermanentSubject creaturesThatPlayerControls = Subjects.controlledBy(
                Subjects.controllerOf(Subjects.target()), new PermanentIsCreaturePredicate());
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target creature an opponent controls"
        )).addSpellEffect(DealDamageEffect.toEachOther(Subjects.target(), creaturesThatPlayerControls,
                new SourcePower()))
                .addSpellEffect(DealDamageEffect.toTargetCreature(new SourcePower())
                        .dealtBy(Subjects.except(creaturesThatPlayerControls, Subjects.targetDeclaredElsewhere())));
    }
}
