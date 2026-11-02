package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "90")
public class BurnBurnTreeAndFern extends Card {

    public BurnBurnTreeAndFern() {
        PermanentPredicate opponentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.chapter(1), new DealDamageToTargetCreatureEffect(6, opponentCreature));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(
                        opponentCreature, "Target must be a creature an opponent controls")));

        PermanentPredicate opponentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.chapter(2), new DestroyTargetPermanentEffect(opponentArtifact));
        setSagaChapterTargetFilter(2, Set.of(
                new PermanentPredicateTargetFilter(
                        opponentArtifact, "Target must be an artifact an opponent controls")));

        addTrigger(Triggers.chapter(3), new AwardManaEffect(ManaColor.RED));
        addTrigger(Triggers.chapter(4), new AwardManaEffect(ManaColor.RED));
    }
}
