package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "8")
public class FallajiAntiquarian extends Card {

    public FallajiAntiquarian() {
        var anotherNontokenCreatureOrArtifactYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsArtifactPredicate())),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        target(1, 1).addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ConjureCardsEffect.duplicateOfTarget(anotherNontokenCreatureOrArtifactYouControl,
                        ConjureDestination.intoGraveyard()),
                new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CONJURE),
                        List.of(Card.unearthAbility("{1}{R}").withZones(AbilityZones.GRAVEYARD)))));
    }
}
